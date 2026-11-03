package com.tutorhub.request.dto;
import jakarta.validation.constraints.*;
public record ApplicationInput(@Size(max=1000) String message, @NotNull @PositiveOrZero Long proposedRate) {}
