package com.tutorhub.booking.dto;

import java.time.Instant;

public record RecurringBookingFailure(Instant startAt, String code, String message) {}
