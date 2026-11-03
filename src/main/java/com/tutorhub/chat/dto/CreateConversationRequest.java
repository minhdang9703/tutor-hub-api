package com.tutorhub.chat.dto;

import jakarta.validation.constraints.NotNull;

public record CreateConversationRequest(@NotNull Long participantId, Long requestId) {}
