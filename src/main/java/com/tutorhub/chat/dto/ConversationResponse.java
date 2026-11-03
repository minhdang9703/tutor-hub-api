package com.tutorhub.chat.dto;

import java.time.Instant;

public record ConversationResponse(Long id, Long parentId, Long tutorId, Long requestId, Instant lastMessageAt, Instant createdAt) {}
