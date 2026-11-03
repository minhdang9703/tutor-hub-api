package com.tutorhub.booking;

import static org.junit.jupiter.api.Assertions.*;

import com.tutorhub.booking.entity.BookingStatus;
import com.tutorhub.booking.service.BookingStateMachine;
import com.tutorhub.common.exception.BusinessRuleException;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class BookingStateMachineTest {
    private final BookingStateMachine states = new BookingStateMachine();
    @Test void permitsOnlyTheDocumentedTransitions() {
        Instant past = Instant.now().minusSeconds(1);
        assertEquals(BookingStatus.CONFIRMED, states.transition(BookingStatus.PENDING, "confirm", past, Instant.now()));
        assertEquals(BookingStatus.CANCELLED, states.transition(BookingStatus.CONFIRMED, "cancel", past, Instant.now()));
        assertEquals(BookingStatus.COMPLETED, states.transition(BookingStatus.CONFIRMED, "complete", past, Instant.now()));
        assertThrows(BusinessRuleException.class, () -> states.transition(BookingStatus.PENDING, "complete", past, Instant.now()));
        assertThrows(BusinessRuleException.class, () -> states.transition(BookingStatus.CANCELLED, "confirm", past, Instant.now()));
    }
}
