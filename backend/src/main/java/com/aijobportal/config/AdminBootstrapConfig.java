package com.aijobportal.config;

import com.aijobportal.auth.entity.User;
import com.aijobportal.auth.repository.UserRepository;
import com.aijobportal.common.domain.AccountStatus;
import com.aijobportal.common.domain.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminBootstrapConfig {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrapConfig.class);

    @Bean
    CommandLineRunner bootstrapAdmin(
            UserRepository users,
            PasswordEncoder encoder,
            @Value("${app.bootstrap-admin.email:}") String email,
            @Value("${app.bootstrap-admin.password:}") String password,
            @Value("${app.bootstrap-admin.name:Admin}") String name
    ) {
        return args -> {
            if (email.isBlank() || password.isBlank()) {
                return;
            }
            if (users.countByRole(Role.ADMIN) > 0) {
                return;
            }
            User admin = new User();
            admin.setName(name.isBlank() ? "Admin" : name.trim());
            admin.setEmail(email.trim().toLowerCase());
            admin.setPasswordHash(encoder.encode(password));
            admin.setRole(Role.ADMIN);
            admin.setAccountStatus(AccountStatus.ACTIVE);
            users.save(admin);
            log.info("Created bootstrap admin account for {}", admin.getEmail());
        };
    }
}
