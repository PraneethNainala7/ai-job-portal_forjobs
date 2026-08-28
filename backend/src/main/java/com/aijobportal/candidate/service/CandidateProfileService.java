package com.aijobportal.candidate.service;

import com.aijobportal.auth.entity.User;
import com.aijobportal.auth.repository.UserRepository;
import com.aijobportal.auth.service.AccountStatusRules;
import com.aijobportal.candidate.dto.CandidateProfileResponse;
import com.aijobportal.candidate.dto.CandidateProfileUpdateRequest;
import com.aijobportal.candidate.dto.ResumeAnalysisResponse;
import com.aijobportal.candidate.entity.CandidateProfile;
import com.aijobportal.candidate.entity.Resume;
import com.aijobportal.candidate.mapper.CandidateMapper;
import com.aijobportal.candidate.repository.CandidateProfileRepository;
import com.aijobportal.candidate.repository.ResumeRepository;
import com.aijobportal.common.domain.ResumeStatus;
import com.aijobportal.common.exception.ApiException;
import com.aijobportal.common.util.Lists;
import com.aijobportal.config.security.AuthPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;

@Service
public class CandidateProfileService {

    private static final Logger log = LoggerFactory.getLogger(CandidateProfileService.class);

    private final CandidateProfileRepository profileRepository;
    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final Path uploadDir;

    public CandidateProfileService(
            CandidateProfileRepository profileRepository,
            ResumeRepository resumeRepository,
            UserRepository userRepository,
            @Value("${app.upload-dir}") String uploadDir
    ) {
        this.profileRepository = profileRepository;
        this.resumeRepository = resumeRepository;
        this.userRepository = userRepository;
        this.uploadDir = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    @Transactional
    public CandidateProfile requireProfile(AuthPrincipal principal) {
        AccountStatusRules.requireCandidateActive(principal.role(), principal.accountStatus());
        return profileRepository.findByUserId(principal.id()).orElseGet(() -> {
            User user = userRepository.findById(principal.id()).orElseThrow(() -> ApiException.unauthorized("Sign-in required."));
            CandidateProfile created = new CandidateProfile();
            created.setUser(user);
            return profileRepository.save(created);
        });
    }

    @Transactional
    public CandidateProfileResponse getProfile(AuthPrincipal principal) {
        return CandidateMapper.toProfile(requireProfile(principal));
    }

    @Transactional
    public CandidateProfileResponse updateProfile(AuthPrincipal principal, CandidateProfileUpdateRequest request) {
        CandidateProfile profile = requireProfile(principal);
        String fullName = request.fullName() == null ? profile.getUser().getName() : request.fullName().trim();
        if (fullName.isBlank()) {
            throw ApiException.badRequest("Full name is required.");
        }
        profile.getUser().setName(fullName);
        profile.setPhone(trim(request.phone()));
        profile.setLocation(trim(request.location()));
        profile.setTitle(trim(request.title()));
        profile.setExperience(trim(request.experience()));
        if (request.skills() != null) {
            profile.setSkills(Lists.copy(request.skills()));
        }
        if (request.education() != null) {
            profile.setEducation(Lists.copy(request.education()));
        }
        if (request.certifications() != null) {
            profile.setCertifications(Lists.copy(request.certifications()));
        }
        profile.setLinkedinUrl(trim(request.linkedinUrl()));
        profile.setPortfolioUrl(trim(request.portfolioUrl()));
        return CandidateMapper.toProfile(profile);
    }

    @Transactional
    public ResumeAnalysisResponse getResume(AuthPrincipal principal) {
        requireProfile(principal);
        return resumeRepository.findByCandidateId(principal.id()).map(CandidateMapper::toResume).orElse(null);
    }

    @Transactional(readOnly = true)
    public CandidateProfileResponse getProfileByUserId(String userId) {
        CandidateProfile profile = profileRepository.findByUserId(userId).orElse(null);
        if (profile == null) {
            User user = userRepository.findById(userId).orElseThrow(() -> ApiException.notFound("Candidate not found."));
            return new CandidateProfileResponse(
                    user.getId(), user.getName(), user.getEmail(), null, null, null, null,
                    List.of(), List.of(), List.of(), null, null
            );
        }
        Resume resume = resumeRepository.findByCandidateId(userId).orElse(null);
        return CandidateMapper.withResumeSkills(CandidateMapper.toProfile(profile), resume);
    }

    @Transactional
    public ResumeAnalysisResponse uploadResume(AuthPrincipal principal, MultipartFile file) {
        CandidateProfile profile = requireProfile(principal);
        if (file == null || file.isEmpty()) {
            throw ApiException.badRequest("Choose a PDF, DOC, or DOCX file.");
        }
        if (file.getSize() > 5L * 1024 * 1024) {
            throw ApiException.badRequest("Resume must be smaller than 5 MB.");
        }
        String original = file.getOriginalFilename() == null ? "resume.pdf" : file.getOriginalFilename();
        if (!original.toLowerCase(Locale.ROOT).matches(".*\\.(pdf|doc|docx)$")) {
            throw ApiException.badRequest("Upload a PDF, DOC, or DOCX file.");
        }
        try {
            Files.createDirectories(uploadDir);
            String stored = principal.id() + "-" + original.replaceAll("[^a-zA-Z0-9._-]", "_");
            Path target = uploadDir.resolve(stored).normalize();
            if (!target.startsWith(uploadDir)) {
                throw ApiException.badRequest("Upload a PDF, DOC, or DOCX file.");
            }
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
            Resume resume = resumeRepository.findByCandidateId(principal.id()).orElseGet(Resume::new);
            resume.setCandidate(profile.getUser());
            resume.setFileName(original);
            resume.setFileType(extension(original));
            resume.setFileUrl(target.toString());
            resume.setStatus(ResumeStatus.PROCESSING);
            resume.setError(null);
            resume.setParsedData(null);
            resumeRepository.save(resume);
            return CandidateMapper.toResume(resume);
        } catch (IOException ex) {
            log.error("Failed to store resume for candidate {}", principal.id(), ex);
            throw ApiException.badRequest("Could not store the resume file.");
        }
    }

    @Transactional
    public CandidateProfileResponse scoringProfile(AuthPrincipal principal) {
        CandidateProfile profile = requireProfile(principal);
        Resume resume = resumeRepository.findByCandidateId(principal.id()).orElse(null);
        return CandidateMapper.withResumeSkills(CandidateMapper.toProfile(profile), resume);
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }

    private static String extension(String name) {
        int dot = name.lastIndexOf('.');
        return dot < 0 ? null : name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
