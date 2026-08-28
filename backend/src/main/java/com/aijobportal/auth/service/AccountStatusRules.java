package com.aijobportal.auth.service;

import com.aijobportal.common.domain.AccountStatus;
import com.aijobportal.common.domain.Role;
import com.aijobportal.common.exception.ApiException;

import java.util.Set;

public final class AccountStatusRules {

    private AccountStatusRules() {
    }

    public static void requireCandidateActive(Role role, AccountStatus status) {
        if (role != Role.CANDIDATE) {
            throw ApiException.unauthorized("Candidate sign-in required.");
        }
        if (status != AccountStatus.ACTIVE) {
            throw ApiException.forbidden("This account cannot use candidate features.");
        }
    }

    public static void requireEmployer(Role role) {
        if (role != Role.EMPLOYER) {
            throw ApiException.unauthorized("Employer sign-in required.");
        }
    }

    public static void requireActiveEmployer(Role role, AccountStatus status) {
        requireEmployer(role);
        if (status != AccountStatus.ACTIVE) {
            throw ApiException.forbidden("This employer account cannot use hiring tools yet.");
        }
    }

    public static void validateTransition(Role role, AccountStatus from, AccountStatus to, String action) {
        if (role == Role.ADMIN) {
            throw ApiException.badRequest("Admin accounts cannot be changed here.");
        }
        if (from == to) {
            return;
        }
        Set<AccountStatus> allowed = switch (action) {
            case "APPROVE_EMPLOYER" -> Set.of(AccountStatus.PENDING, AccountStatus.ON_HOLD, AccountStatus.INACTIVE);
            case "REJECT_EMPLOYER" -> Set.of(AccountStatus.PENDING);
            case "HOLD_EMPLOYER", "HOLD_USER" -> Set.of(AccountStatus.ACTIVE);
            case "ACTIVATE_EMPLOYER", "ACTIVATE_USER" -> Set.of(AccountStatus.ON_HOLD, AccountStatus.INACTIVE, AccountStatus.PENDING);
            case "DEACTIVATE_USER" -> Set.of(AccountStatus.ACTIVE, AccountStatus.ON_HOLD);
            case "RESUBMIT_EMPLOYER" -> Set.of(AccountStatus.REJECTED);
            default -> Set.of();
        };
        if (!allowed.contains(from) && !compatible(from, to)) {
            throw ApiException.badRequest("This account status change is not allowed.");
        }
        if (role == Role.CANDIDATE && to == AccountStatus.PENDING) {
            throw ApiException.badRequest("This account status change is not allowed.");
        }
    }

    private static boolean compatible(AccountStatus from, AccountStatus to) {
        return (from == AccountStatus.ACTIVE && (to == AccountStatus.ON_HOLD || to == AccountStatus.INACTIVE))
                || (from == AccountStatus.ON_HOLD && (to == AccountStatus.ACTIVE || to == AccountStatus.INACTIVE))
                || (from == AccountStatus.INACTIVE && to == AccountStatus.ACTIVE)
                || (from == AccountStatus.PENDING && (to == AccountStatus.ACTIVE || to == AccountStatus.REJECTED))
                || (from == AccountStatus.REJECTED && to == AccountStatus.PENDING);
    }
}
