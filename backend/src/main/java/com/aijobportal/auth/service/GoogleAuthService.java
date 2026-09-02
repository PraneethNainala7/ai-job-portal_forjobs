package com.aijobportal.auth.service;

import com.aijobportal.auth.entity.User;
import com.aijobportal.auth.repository.UserRepository;
import com.aijobportal.candidate.entity.CandidateProfile;
import com.aijobportal.candidate.repository.CandidateProfileRepository;
import com.aijobportal.common.domain.AccountStatus;
import com.aijobportal.common.domain.Role;
import com.aijobportal.common.exception.ApiException;
import com.aijobportal.employer.repository.CompanyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GoogleAuthService {

    public static final String NEEDS_ROLE_CODE = "NEEDS_ROLE";

    private final GoogleTokenVerifier googleTokenVerifier;
    private final UserRepository userRepository;
    private final CandidateProfileRepository candidateProfileRepository;
    private final CompanyRepository companyRepository;

    public GoogleAuthService(
            GoogleTokenVerifier googleTokenVerifier,
            UserRepository userRepository,
            CandidateProfileRepository candidateProfileRepository,
            CompanyRepository companyRepository
    ) {
        this.googleTokenVerifier = googleTokenVerifier;
        this.userRepository = userRepository;
        this.candidateProfileRepository = candidateProfileRepository;
        this.companyRepository = companyRepository;
    }

    @Transactional
    public GoogleAuthResult authenticate(String idToken, String roleValue) {
        GoogleTokenVerifier.GoogleUserInfo googleUser = googleTokenVerifier.verify(idToken);

        User existingBySub = userRepository.findByGoogleSub(googleUser.sub()).orElse(null);
        if (existingBySub != null) {
            return loginExisting(existingBySub);
        }

        User existingByEmail = userRepository.findByEmailIgnoreCase(googleUser.email()).orElse(null);
        if (existingByEmail != null) {
            return linkAndLogin(existingByEmail, googleUser);
        }

        Role role = parseRole(roleValue);
        if (role == null) {
            throw ApiException.conflict("Choose whether you are signing up as a candidate or employer.", NEEDS_ROLE_CODE);
        }
        if (role == Role.ADMIN) {
            throw ApiException.badRequest("Admin accounts cannot be self-registered.");
        }
        if (role == Role.EMPLOYER) {
            throw ApiException.badRequest("Employers must register with a company email and password.");
        }

        User user = new User();
        user.setName(googleUser.name());
        user.setEmail(googleUser.email());
        user.setGoogleSub(googleUser.sub());
        user.setRole(role);
        user.setAccountStatus(AccountStatus.ACTIVE);
        userRepository.save(user);

        CandidateProfile profile = new CandidateProfile();
        profile.setUser(user);
        candidateProfileRepository.save(profile);
        return new GoogleAuthResult(user, false);
    }

    private GoogleAuthResult linkAndLogin(User user, GoogleTokenVerifier.GoogleUserInfo googleUser) {
        if (user.getGoogleSub() != null && !user.getGoogleSub().equals(googleUser.sub())) {
            throw ApiException.conflict("This email is linked to a different Google account.");
        }
        if (user.getGoogleSub() == null) {
            user.setGoogleSub(googleUser.sub());
        }
        if (blank(user.getName()) && !blank(googleUser.name())) {
            user.setName(googleUser.name());
        }
        userRepository.save(user);
        return loginExisting(user);
    }

    private GoogleAuthResult loginExisting(User user) {
        if (user.getRole() == Role.ADMIN) {
            throw ApiException.forbidden("Admin accounts cannot use Google sign-in.");
        }
        if (user.getAccountStatus() == AccountStatus.INACTIVE) {
            throw ApiException.forbidden("This account is inactive.");
        }
        boolean needsOnboarding = user.getRole() == Role.EMPLOYER
                && companyRepository.findByEmployerId(user.getId()).isEmpty();
        return new GoogleAuthResult(user, needsOnboarding);
    }

    private static Role parseRole(String roleValue) {
        if (roleValue == null || roleValue.isBlank()) {
            return null;
        }
        try {
            return Role.valueOf(roleValue.trim());
        } catch (IllegalArgumentException ex) {
            throw ApiException.badRequest("Role is required.");
        }
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    public record GoogleAuthResult(User user, boolean needsEmployerOnboarding) {
    }
}
