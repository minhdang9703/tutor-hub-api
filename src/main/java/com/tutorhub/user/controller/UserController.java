package com.tutorhub.user.controller;

import com.tutorhub.common.security.CurrentUser;
import com.tutorhub.user.dto.*;
import com.tutorhub.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController @RequestMapping("/api/v1/users/me")
public class UserController {
    private final UserService users; private final CurrentUser currentUser;
    public UserController(UserService users, CurrentUser currentUser) { this.users = users; this.currentUser = currentUser; }
    @GetMapping UserResponse me() { return users.me(currentUser.require()); }
    @PatchMapping UserResponse update(@Valid @RequestBody UpdateUserRequest request) { return users.update(currentUser.require(), request); }
    @PostMapping("/avatar") UserResponse avatar(@RequestPart("file") MultipartFile file) { return users.uploadAvatar(currentUser.require(), file); }
    @DeleteMapping @ResponseStatus(HttpStatus.NO_CONTENT) void delete() { users.delete(currentUser.require()); }
}
