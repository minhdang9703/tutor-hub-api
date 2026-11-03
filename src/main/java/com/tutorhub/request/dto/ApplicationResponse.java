package com.tutorhub.request.dto;
import java.time.Instant;
public record ApplicationResponse(Long id, Long requestId, Long tutorId, String message, Long proposedRate, String status, Instant createdAt, Instant updatedAt) {}
