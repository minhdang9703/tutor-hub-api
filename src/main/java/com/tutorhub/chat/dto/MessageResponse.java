package com.tutorhub.chat.dto;

import java.time.Instant;

public record MessageResponse(Long id, Long senderId, String content, Instant readAt, Instant createdAt, boolean caution) {}
