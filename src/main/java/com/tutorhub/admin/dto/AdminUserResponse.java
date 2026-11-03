package com.tutorhub.admin.dto;

import java.time.Instant;

public record AdminUserResponse(Long id, String email, String phone, String fullName, String role, String status, Instant createdAt) {}
