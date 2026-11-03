package com.tutorhub.user.dto;
import jakarta.validation.constraints.*;
public record UpdateUserRequest(@NotBlank @Size(max = 120) String fullName, @Size(max = 20) String phone) {}
