package com.tutorhub.admin.dto;

import java.time.Instant;

public record AuditLogResponse(Long id, Long actorId, String action, String entityType, Long entityId, String ipAddress, Instant createdAt) {}
