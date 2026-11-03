package com.tutorhub.auth.dto;
import com.tutorhub.user.entity.Role;
import jakarta.validation.constraints.*;
public record RegisterRequest(@NotBlank @Email @Size(max = 120) String email, @NotBlank @Size(min = 8, max = 72) String password, @NotBlank @Size(max = 120) String fullName, @Size(max = 20) String phone, @NotNull Role role) {}
