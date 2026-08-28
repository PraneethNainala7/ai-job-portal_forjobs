package com.aijobportal.auth.service;

import com.aijobportal.common.domain.AccountStatus;
import com.aijobportal.common.domain.Role;
import com.aijobportal.common.exception.ApiException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountStatusRulesTest {

    @Test
    void candidateMustBeActive() {
        ApiException ex = assertThrows(ApiException.class,
                () -> AccountStatusRules.requireCandidateActive(Role.CANDIDATE, AccountStatus.ON_HOLD));
        assertEquals(403, ex.getStatus().value());
    }

    @Test
    void pendingEmployerCannotUseHiringTools() {
        ApiException ex = assertThrows(ApiException.class,
                () -> AccountStatusRules.requireActiveEmployer(Role.EMPLOYER, AccountStatus.PENDING));
        assertEquals(403, ex.getStatus().value());
    }

    @Test
    void approvePendingEmployer() {
        assertDoesNotThrow(() -> AccountStatusRules.validateTransition(
                Role.EMPLOYER, AccountStatus.PENDING, AccountStatus.ACTIVE, "APPROVE_EMPLOYER"));
    }

    @Test
    void cannotChangeAdmin() {
        assertThrows(ApiException.class, () -> AccountStatusRules.validateTransition(
                Role.ADMIN, AccountStatus.ACTIVE, AccountStatus.INACTIVE, "DEACTIVATE_USER"));
    }
}
