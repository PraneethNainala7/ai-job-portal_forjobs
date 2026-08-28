package com.aijobportal.audit.service;

import com.aijobportal.audit.entity.AuditLog;
import com.aijobportal.audit.repository.AuditLogRepository;
import com.aijobportal.config.security.AuthPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void record(
            AuthPrincipal admin,
            String action,
            String targetType,
            String targetId,
            String previousStatus,
            String newStatus,
            String reason
    ) {
        AuditLog log = new AuditLog();
        log.setAdminId(admin.id());
        log.setAdminName(admin.name());
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setPreviousStatus(previousStatus);
        log.setNewStatus(newStatus);
        log.setReason(reason);
        auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> list() {
        return auditLogRepository.findAllByOrderByCreatedAtDesc();
    }
}
