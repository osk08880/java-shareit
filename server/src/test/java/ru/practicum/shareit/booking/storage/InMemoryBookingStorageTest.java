package ru.practicum.shareit.booking.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryBookingStorageTest {

    private InMemoryBookingStorage storage;
    private Item item1;
    private Item item2;
    private Booking booking1;
    private Booking booking2;

    @BeforeEach
    void setUp() {
        storage = new InMemoryBookingStorage();

        User owner = User.builder().id(1L).name("Owner").email("owner@example.com").build();
        item1 = Item.builder().id(1L).name("Item1").owner(owner).available(true).build();
        item2 = Item.builder().id(2L).name("Item2").owner(owner).available(true).build();

        booking1 = Booking.builder().item(item1).booker(owner).build();
        booking2 = Booking.builder().item(item2).booker(owner).build();

        storage.create(booking1);
        storage.create(booking2);
    }

    @Test
    void create_assignsIdAndStoresBooking() {
        Booking booking = Booking.builder().item(item1).booker(User.builder().id(2L).name("User").build()).build();
        Booking created = storage.create(booking);

        assertThat(created.getId()).isNotNull();
        List<Booking> bookings = storage.findByItemId(item1.getId());
        assertThat(bookings).contains(created);
    }

    @Test
    void findByItemId_existing_returnsBookings() {
        List<Booking> bookings = storage.findByItemId(item1.getId());
        assertThat(bookings).hasSize(1).contains(booking1);
    }

    @Test
    void findByItemId_nonExisting_returnsEmpty() {
        List<Booking> bookings = storage.findByItemId(999L);
        assertThat(bookings).isEmpty();
    }

    @Test
    void findByItemId_nullItemId_returnsEmpty() {
        List<Booking> bookings = storage.findByItemId(null);
        assertThat(bookings).isEmpty();
    }
}
