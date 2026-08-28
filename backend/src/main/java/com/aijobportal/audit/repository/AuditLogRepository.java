package com.aijobportal.audit.repository;

import com.aijobportal.audit.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, String> {

    List<AuditLog> findAllByOrderByCreatedAtDesc();
}
