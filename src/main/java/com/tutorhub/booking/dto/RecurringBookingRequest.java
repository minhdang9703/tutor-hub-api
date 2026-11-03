package com.tutorhub.booking.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RecurringBookingRequest(@Valid @NotNull CreateBookingRequest booking, @Min(1) @Max(52) int occurrences) {}
