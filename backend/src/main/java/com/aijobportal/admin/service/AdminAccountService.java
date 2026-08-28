package com.aijobportal.admin.service;

import com.aijobportal.audit.service.AuditService;
import com.aijobportal.auth.dto.UserResponse;
import com.aijobportal.auth.entity.User;
import com.aijobportal.auth.mapper.UserMapper;
import com.aijobportal.auth.repository.UserRepository;
import com.aijobportal.auth.service.AccountStatusRules;
import com.aijobportal.common.domain.AccountStatus;
import com.aijobportal.common.domain.Role;
import com.aijobportal.common.exception.ApiException;
import com.aijobportal.config.security.AuthPrincipal;
import com.aijobportal.employer.repository.CompanyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class AdminAccountService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final AuditService auditService;
    private final AdminQueryService adminQueryService;

    public AdminAccountService(
            UserRepository userRepository,
            CompanyRepository companyRepository,
            AuditService auditService,
            AdminQueryService adminQueryService
    ) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.auditService = auditService;
        this.adminQueryService = adminQueryService;
    }

    @Transactional
    public UserResponse changeStatus(AuthPrincipal admin, String id, AccountStatus next, String action, String reason) {
        User user = userRepository.findById(id).orElseThrow(() -> ApiException.notFound("Account not found."));
        AccountStatusRules.validateTransition(user.getRole(), user.getAccountStatus(), next, action);
        AccountStatus previous = user.getAccountStatus();
        user.setAccountStatus(next);
        user.setStatusChangedBy(admin.id());
        user.setStatusChangedAt(Instant.now());
        if (next == AccountStatus.REJECTED) {
            user.setStatusReason(reason);
        }
        if (next == AccountStatus.ON_HOLD) {
            user.setHoldReason(reason);
        }
        if (next == AccountStatus.ACTIVE) {
            user.setStatusReason(null);
            user.setHoldReason(null);
        }
        auditService.record(admin, action, user.getRole().name(), user.getId(), previous.name(), next.name(), reason);
        return UserMapper.toAdminAccount(user, companyRepository.findByEmployerId(user.getId()).orElse(null));
    }

    @Transactional
    public UserResponse resubmitEmployer(AuthPrincipal admin, String id) {
        User user = adminQueryService.requireRole(id, Role.EMPLOYER, "Employer not found.");
        if (user.getAccountStatus() != AccountStatus.REJECTED) {
            throw ApiException.badRequest("Only a rejected registration can be resubmitted.");
        }
        AccountStatus previous = user.getAccountStatus();
        user.setAccountStatus(AccountStatus.PENDING);
        user.setStatusReason(null);
        user.setStatusChangedBy(admin.id());
        user.setStatusChangedAt(Instant.now());
        auditService.record(admin, "RESUBMIT_EMPLOYER", "EMPLOYER", id, previous.name(), AccountStatus.PENDING.name(), null);
        return UserMapper.toAdminAccount(user, companyRepository.findByEmployerId(id).orElse(null));
    }

    @Transactional
    public void deleteUser(AuthPrincipal admin, String id) {
        User user = userRepository.findById(id).orElseThrow(() -> ApiException.notFound("Account not found."));
        if (user.getRole() == Role.ADMIN) {
            throw ApiException.badRequest("Admin accounts cannot be deleted.");
        }
        auditService.record(admin, "DELETE_USER", user.getRole().name(), user.getId(), user.getAccountStatus().name(), null, null);
        userRepository.delete(user);
    }
}
