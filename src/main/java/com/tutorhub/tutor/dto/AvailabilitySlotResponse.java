package com.tutorhub.tutor.dto;
import java.time.LocalTime;
public record AvailabilitySlotResponse(Long id,short dayOfWeek,LocalTime startTime,LocalTime endTime) {}
