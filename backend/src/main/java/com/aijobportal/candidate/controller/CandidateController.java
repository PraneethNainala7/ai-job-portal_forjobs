package com.aijobportal.candidate.controller;

import com.aijobportal.application.dto.ApplicationItemsResponse;
import com.aijobportal.application.service.ApplicationService;
import com.aijobportal.candidate.dto.CandidateProfileResponse;
import com.aijobportal.candidate.dto.CandidateProfileUpdateRequest;
import com.aijobportal.candidate.dto.ResumeEnvelope;
import com.aijobportal.candidate.service.CandidateProfileService;
import com.aijobportal.config.security.AuthCookieService;
import com.aijobportal.config.security.SecurityUtils;
import com.aijobportal.employer.repository.CompanyRepository;
import com.aijobportal.auth.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/candidate")
public class CandidateController {

    private final CandidateProfileService candidateProfileService;
    private final ApplicationService applicationService;
    private final AuthCookieService authCookieService;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    public CandidateController(
            CandidateProfileService candidateProfileService,
            ApplicationService applicationService,
            AuthCookieService authCookieService,
            UserRepository userRepository,
            CompanyRepository companyRepository
    ) {
        this.candidateProfileService = candidateProfileService;
        this.applicationService = applicationService;
        this.authCookieService = authCookieService;
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
    }

    @GetMapping("/profile")
    public CandidateProfileResponse getProfile() {
        return candidateProfileService.getProfile(SecurityUtils.requireUser());
    }

    @PutMapping("/profile")
    public CandidateProfileResponse updateProfile(
            @RequestBody CandidateProfileUpdateRequest request,
            HttpServletResponse response
    ) {
        var principal = SecurityUtils.requireUser();
        CandidateProfileResponse profile = candidateProfileService.updateProfile(principal, request);
        userRepository.findById(principal.id()).ifPresent(user ->
                authCookieService.setSession(response, user, companyRepository.findByEmployerId(user.getId()).orElse(null)));
        return profile;
    }

    @GetMapping("/resume")
    public ResumeEnvelope getResume() {
        return new ResumeEnvelope(candidateProfileService.getResume(SecurityUtils.requireUser()));
    }

    @PostMapping("/resume")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResumeEnvelope uploadResume(@RequestPart("resume") MultipartFile resume) {
        return new ResumeEnvelope(candidateProfileService.uploadResume(SecurityUtils.requireUser(), resume));
    }

    @GetMapping("/applications")
    public ApplicationItemsResponse applications() {
        return new ApplicationItemsResponse(applicationService.listForCandidate(SecurityUtils.requireUser()));
    }
}
