package com.tutorhub.booking.dto;

import java.util.List;

public record RecurringBookingResponse(List<BookingResponse> created, List<RecurringBookingFailure> failed) {}
