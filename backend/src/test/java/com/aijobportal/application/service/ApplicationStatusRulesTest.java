package com.aijobportal.application.service;

import com.aijobportal.common.domain.ApplicationStatus;
import com.aijobportal.common.exception.ApiException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ApplicationStatusRulesTest {

    @Test
    void shortlistAllowedFromApplied() {
        assertDoesNotThrow(() -> ApplicationStatusRules.requireCanShortlist(ApplicationStatus.APPLIED));
    }

    @Test
    void shortlistBlockedFromRejected() {
        ApiException ex = assertThrows(ApiException.class,
                () -> ApplicationStatusRules.requireCanShortlist(ApplicationStatus.REJECTED));
        assertEquals(400, ex.getStatus().value());
    }

    @Test
    void shortlistBlockedFromShortlisted() {
        ApiException ex = assertThrows(ApiException.class,
                () -> ApplicationStatusRules.requireCanShortlist(ApplicationStatus.SHORTLISTED));
        assertEquals(400, ex.getStatus().value());
    }

    @Test
    void rejectAllowedFromAppliedShortlistedAndInterview() {
        assertDoesNotThrow(() -> ApplicationStatusRules.requireCanReject(ApplicationStatus.APPLIED));
        assertDoesNotThrow(() -> ApplicationStatusRules.requireCanReject(ApplicationStatus.SHORTLISTED));
        assertDoesNotThrow(() -> ApplicationStatusRules.requireCanReject(ApplicationStatus.INTERVIEW));
    }

    @Test
    void rejectBlockedFromRejected() {
        ApiException ex = assertThrows(ApiException.class,
                () -> ApplicationStatusRules.requireCanReject(ApplicationStatus.REJECTED));
        assertEquals(400, ex.getStatus().value());
    }

    @Test
    void rejectBlockedFromSelected() {
        ApiException ex = assertThrows(ApiException.class,
                () -> ApplicationStatusRules.requireCanReject(ApplicationStatus.SELECTED));
        assertEquals(400, ex.getStatus().value());
    }
}
