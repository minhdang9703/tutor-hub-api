package com.tutorhub.tutor.dto;
import jakarta.validation.constraints.*;
import java.time.LocalTime;
public record AvailabilitySlotRequest(@Min(1)@Max(7) short dayOfWeek,@NotNull LocalTime startTime,@NotNull LocalTime endTime) {}
