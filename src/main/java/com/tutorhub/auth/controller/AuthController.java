package com.tutorhub.auth.controller;

import com.tutorhub.auth.dto.*;
import com.tutorhub.auth.service.AuthRateLimiter;
import com.tutorhub.auth.service.AuthService;
import com.tutorhub.common.security.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService auth; private final AuthRateLimiter rateLimiter; private final CurrentUser currentUser;
    public AuthController(AuthService auth, AuthRateLimiter rateLimiter, CurrentUser currentUser) { this.auth = auth; this.rateLimiter = rateLimiter; this.currentUser = currentUser; }
    @PostMapping("/register") @ResponseStatus(HttpStatus.CREATED) RegisterResponse register(@Valid @RequestBody RegisterRequest request) { return auth.register(request); }
    @PostMapping("/login") AuthResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest http) { rateLimiter.check("login", ip(http)); return auth.login(request, http.getHeader("User-Agent"), ip(http)); }
    @PostMapping("/refresh") AuthResponse refresh(@Valid @RequestBody RefreshRequest request, HttpServletRequest http) { return auth.refresh(request.refreshToken(), http.getHeader("User-Agent"), ip(http)); }
    @PostMapping("/logout") @ResponseStatus(HttpStatus.NO_CONTENT) void logout(@Valid @RequestBody RefreshRequest request) { auth.logout(request.refreshToken()); }
    @PostMapping("/verify-email") @ResponseStatus(HttpStatus.NO_CONTENT) void verify(@Valid @RequestBody TokenRequest request) { auth.verifyEmail(request.token()); }
    @PostMapping("/resend-verification") @ResponseStatus(HttpStatus.NO_CONTENT) void resend(@Valid @RequestBody EmailRequest request) { auth.resendVerification(request.email()); }
    @PostMapping("/forgot-password") @ResponseStatus(HttpStatus.NO_CONTENT) void forgot(@Valid @RequestBody EmailRequest request, HttpServletRequest http) { rateLimiter.check("forgot", ip(http)); auth.forgotPassword(request.email()); }
    @PostMapping("/reset-password") @ResponseStatus(HttpStatus.NO_CONTENT) void reset(@Valid @RequestBody ResetPasswordRequest request) { auth.resetPassword(request); }
    @PostMapping("/change-password") @ResponseStatus(HttpStatus.NO_CONTENT) void change(@Valid @RequestBody ChangePasswordRequest request) { auth.changePassword(currentUser.require(), request); }
    private String ip(HttpServletRequest request) { return request.getRemoteAddr(); }
}
