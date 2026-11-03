package com.tutorhub.chat.dto;

import java.util.List;

public record MessagePage(List<MessageResponse> content, Long nextBeforeId) {}
