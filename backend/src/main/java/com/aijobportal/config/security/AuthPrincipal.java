package com.aijobportal.config.security;

import com.aijobportal.common.domain.AccountStatus;
import com.aijobportal.common.domain.Role;

public record AuthPrincipal(
        String id,
        String email,
        String name,
        Role role,
        AccountStatus accountStatus
) {
}
