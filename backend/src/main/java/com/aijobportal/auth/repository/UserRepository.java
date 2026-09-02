package com.aijobportal.auth.repository;

import com.aijobportal.auth.entity.User;
import com.aijobportal.common.domain.AccountStatus;
import com.aijobportal.common.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByGoogleSub(String googleSub);

    boolean existsByEmailIgnoreCase(String email);

    List<User> findByRoleOrderByCreatedAtDesc(Role role);

    List<User> findByRoleAndAccountStatus(Role role, AccountStatus status);

    long countByRole(Role role);

    long countByRoleAndAccountStatus(Role role, AccountStatus status);
}
