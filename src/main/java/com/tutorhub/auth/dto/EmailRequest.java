package com.tutorhub.auth.dto;
import jakarta.validation.constraints.*;
public record EmailRequest(@NotBlank @Email String email) {}
