package com.aijobportal.application.service;

import com.aijobportal.common.domain.ApplicationStatus;
import com.aijobportal.common.exception.ApiException;

public final class ApplicationStatusRules {

    private ApplicationStatusRules() {
    }

    public static void requireCanShortlist(ApplicationStatus from) {
        if (from == ApplicationStatus.REJECTED) {
            throw ApiException.badRequest("A rejected application cannot be shortlisted.");
        }
        if (from == ApplicationStatus.SELECTED) {
            throw ApiException.badRequest("A selected application cannot be shortlisted.");
        }
        if (from != ApplicationStatus.APPLIED) {
            throw ApiException.badRequest("Only new applications can be shortlisted.");
        }
    }

    public static void requireCanReject(ApplicationStatus from) {
        if (from == ApplicationStatus.REJECTED) {
            throw ApiException.badRequest("This application is already rejected.");
        }
        if (from == ApplicationStatus.SELECTED) {
            throw ApiException.badRequest("A selected application cannot be rejected.");
        }
        if (from != ApplicationStatus.APPLIED
                && from != ApplicationStatus.SHORTLISTED
                && from != ApplicationStatus.INTERVIEW) {
            throw ApiException.badRequest("This application cannot be rejected.");
        }
    }
}
