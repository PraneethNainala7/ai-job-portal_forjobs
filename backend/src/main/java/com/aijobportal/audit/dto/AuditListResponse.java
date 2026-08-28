package com.aijobportal.audit.dto;

import com.aijobportal.audit.entity.AuditLog;

import java.util.List;
import java.util.Map;

public record AuditListResponse(List<AuditLog> items, Map<String, Integer> aiUsage) {
}
