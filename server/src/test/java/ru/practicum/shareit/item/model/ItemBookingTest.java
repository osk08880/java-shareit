package ru.practicum.shareit.item.model;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ItemBookingTest {

    @Test
    void setLastBooking_noApprovedBooking_returnsNull() {
        Item item = new Item();
        item.setId(1L);

        Booking rejected = createBooking(1L, item, BookingStatus.REJECTED, LocalDateTime.now().minusDays(5), LocalDateTime.now().minusDays(4));

        item.setLastBooking(List.of(rejected));

        assertThat(item.getLastBooking()).isNull();
    }

    @Test
    void setNextBooking_selectsEarliestFutureApprovedBooking() {
        Item item = new Item();
        item.setId(1L);

        Booking future1 = createBooking(1L, item, BookingStatus.APPROVED, LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(3));
        Booking future2 = createBooking(2L, item, BookingStatus.APPROVED, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));
        Booking futureRejected = createBooking(3L, item, BookingStatus.REJECTED, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        item.setNextBooking(List.of(future1, future2, futureRejected));

        assertThat(item.getNextBooking()).isEqualTo(future2);
    }

    @Test
    void setNextBooking_noFutureApprovedBooking_returnsNull() {
        Item item = new Item();
        item.setId(1L);

        Booking past = createBooking(1L, item, BookingStatus.APPROVED, LocalDateTime.now().minusDays(3), LocalDateTime.now().minusDays(2));
        Booking rejectedFuture = createBooking(2L, item, BookingStatus.REJECTED, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        item.setNextBooking(List.of(past, rejectedFuture));

        assertThat(item.getNextBooking()).isNull();
    }

    private Booking createBooking(Long id, Item item, BookingStatus status, LocalDateTime start, LocalDateTime end) {
        Booking booking = new Booking();
        booking.setId(id);
        booking.setItem(item);
        booking.setStatus(status);
        booking.setStart(start);
        booking.setEnd(end);
        return booking;
    }
}
