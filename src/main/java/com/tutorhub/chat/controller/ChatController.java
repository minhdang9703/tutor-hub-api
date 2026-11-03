package com.tutorhub.chat.controller;

import com.tutorhub.chat.dto.*;
import com.tutorhub.chat.service.ChatService;
import com.tutorhub.common.pagination.PageResponse;
import com.tutorhub.common.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/conversations")
public class ChatController {
    private final ChatService chats; private final CurrentUser current;
    public ChatController(ChatService chats, CurrentUser current) { this.chats = chats; this.current = current; }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) ConversationResponse create(@Valid @RequestBody CreateConversationRequest request) { return chats.create(current.require(), request); }
    @GetMapping PageResponse<ConversationResponse> list(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) { return chats.list(current.require(), Math.max(page, 0), Math.clamp(size, 1, 100)); }
    @GetMapping("/{id}/messages") MessagePage messages(@PathVariable long id, @RequestParam(required = false) Long beforeId, @RequestParam(defaultValue = "50") int size) { return chats.messages(current.require(), id, beforeId, Math.clamp(size, 1, 100)); }
    @PostMapping("/{id}/messages") @ResponseStatus(HttpStatus.CREATED) MessageResponse send(@PathVariable long id, @Valid @RequestBody MessageRequest request) { return chats.send(current.require(), id, request); }
    @PostMapping("/{id}/read") @ResponseStatus(HttpStatus.NO_CONTENT) void read(@PathVariable long id) { chats.markRead(current.require(), id); }
}
