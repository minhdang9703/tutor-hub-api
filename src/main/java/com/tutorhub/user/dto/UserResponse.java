package com.tutorhub.user.dto;
import com.tutorhub.user.entity.Role;
import com.tutorhub.user.entity.UserStatus;
import java.time.Instant;
public record UserResponse(Long id, String email, String phone, String fullName, String avatarKey, Role role, UserStatus status, Instant emailVerifiedAt) {}
