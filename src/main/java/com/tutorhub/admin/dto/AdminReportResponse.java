package com.tutorhub.admin.dto;

import java.time.Instant;

public record AdminReportResponse(Long id, Long reporterId, String targetType, Long targetId, String reason, String description, String status, Long resolvedBy, String resolution, Instant createdAt, Instant resolvedAt) {}
