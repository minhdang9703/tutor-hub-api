package com.tutorhub.request.dto;

import java.time.Instant;

public record TutorRequestResponse(Long id, Long parentId, Long childId, Integer subjectId, Short grade, Integer districtId,
        String mode, Long budgetMin, Long budgetMax, Short sessionsPerWeek, String scheduleNote, String description,
        String status, Instant expiresAt, Instant createdAt, Instant updatedAt) {}
