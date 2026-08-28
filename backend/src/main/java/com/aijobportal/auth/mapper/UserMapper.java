package com.aijobportal.auth.mapper;

import com.aijobportal.auth.dto.UserResponse;
import com.aijobportal.auth.entity.User;
import com.aijobportal.employer.entity.Company;

import java.time.Instant;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserResponse toUser(User user) {
        return toUser(user, user.getCompany(), false);
    }

    public static UserResponse toUser(User user, Company company) {
        return toUser(user, company, false);
    }

    public static UserResponse toAdminAccount(User user, Company company) {
        return toUser(user, company, true);
    }

    private static UserResponse toUser(User user, Company company, boolean admin) {
        String rejection = user.getAccountStatus() != null && user.getAccountStatus().name().equals("REJECTED")
                ? user.getStatusReason()
                : null;
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                user.getAccountStatus().name(),
                company != null ? company.getCompanyName() : null,
                company != null ? company.getDescription() : null,
                company != null ? company.getLocation() : null,
                company != null ? company.getCin() : null,
                company != null ? company.getWebsite() : null,
                rejection,
                admin ? iso(user.getCreatedAt()) : null,
                admin ? user.getHoldReason() : null
        );
    }

    private static String iso(Instant value) {
        return value == null ? null : value.toString();
    }
}
