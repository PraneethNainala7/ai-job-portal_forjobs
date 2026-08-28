package com.aijobportal.auth.service;

import com.aijobportal.auth.dto.LoginRequest;
import com.aijobportal.auth.dto.RegisterRequest;
import com.aijobportal.auth.dto.UserResponse;
import com.aijobportal.auth.entity.User;
import com.aijobportal.auth.mapper.UserMapper;
import com.aijobportal.auth.repository.UserRepository;
import com.aijobportal.candidate.entity.CandidateProfile;
import com.aijobportal.candidate.repository.CandidateProfileRepository;
import com.aijobportal.common.domain.AccountStatus;
import com.aijobportal.common.domain.Role;
import com.aijobportal.common.exception.ApiException;
import com.aijobportal.employer.entity.Company;
import com.aijobportal.employer.repository.CompanyRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final CandidateProfileRepository candidateProfileRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserRepository userRepository,
            CompanyRepository companyRepository,
            CandidateProfileRepository candidateProfileRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.candidateProfileRepository = candidateProfileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email().trim())
                .orElseThrow(() -> ApiException.unauthorized("Incorrect email or password."));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw ApiException.unauthorized("Incorrect email or password.");
        }
        if (user.getAccountStatus() == AccountStatus.INACTIVE) {
            throw ApiException.forbidden("This account is inactive.");
        }
        return user;
    }

    @Transactional
    public User register(RegisterRequest request) {
        Role role;
        try {
            role = Role.valueOf(request.role());
        } catch (IllegalArgumentException ex) {
            throw ApiException.badRequest("Role is required.");
        }
        if (role == Role.ADMIN) {
            throw ApiException.badRequest("Admin accounts cannot be self-registered.");
        }
        if (userRepository.existsByEmailIgnoreCase(request.email().trim())) {
            throw ApiException.conflict("An account with this email already exists.");
        }
        if (role == Role.EMPLOYER) {
            validateEmployer(request);
        }
        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(request.email().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(role);
        user.setAccountStatus(role == Role.EMPLOYER ? AccountStatus.PENDING : AccountStatus.ACTIVE);
        userRepository.save(user);

        if (role == Role.CANDIDATE) {
            CandidateProfile profile = new CandidateProfile();
            profile.setUser(user);
            candidateProfileRepository.save(profile);
        } else {
            Company company = new Company();
            company.setEmployer(user);
            company.setCompanyName(request.companyName().trim());
            company.setDescription(request.companyInformation().trim());
            company.setLocation(request.companyLocation().trim());
            company.setWebsite(blankToNull(request.companyWebsite()));
            company.setCin(request.cin().trim().toUpperCase());
            companyRepository.save(company);
            user.setCompany(company);
        }
        return user;
    }

    @Transactional(readOnly = true)
    public UserResponse toResponse(User user) {
        Company company = user.getRole() == Role.EMPLOYER
                ? companyRepository.findByEmployerId(user.getId()).orElse(null)
                : null;
        return UserMapper.toUser(user, company);
    }

    @Transactional(readOnly = true)
    public User getById(String id) {
        return userRepository.findById(id).orElseThrow(() -> ApiException.unauthorized("Sign-in required."));
    }

    private void validateEmployer(RegisterRequest request) {
        if (blank(request.companyName())) {
            throw ApiException.badRequest("Company name is required.");
        }
        if (blank(request.companyInformation())) {
            throw ApiException.badRequest("Company information is required.");
        }
        if (blank(request.companyLocation())) {
            throw ApiException.badRequest("Company location is required.");
        }
        if (blank(request.cin()) || request.cin().trim().length() < 8) {
            throw ApiException.badRequest("CIN is mandatory.");
        }
        if (companyRepository.existsByCinIgnoreCase(request.cin().trim())) {
            throw ApiException.conflict("An employer with this CIN already exists.");
        }
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private static String blankToNull(String value) {
        return blank(value) ? null : value.trim();
    }
}
