package ru.practicum.shareit.booking.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    private User booker;
    private Item item;
    private Booking booking;

    @BeforeEach
    void setUp() {
        booker = User.builder()
                .name("Alice")
                .email("alice@example.com")
                .build();
        booker = userRepository.save(booker);

        item = Item.builder()
                .name("Drill")
                .description("Powerful drill")
                .available(true)
                .owner(booker)
                .build();
        item = itemRepository.save(item);

        booking = Booking.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.WAITING)
                .booker(booker)
                .item(item)
                .build();
        booking = bookingRepository.save(booking);
    }

    @Test
    void findAllByBookerId_returnsBookings() {
        List<Booking> bookings = bookingRepository.findAllByBookerId(booker.getId(), Sort.by("start").descending());
        assertThat(bookings).isNotEmpty();
        assertThat(bookings.get(0).getBooker()).isEqualTo(booker);
    }

    @Test
    void findAllByBookerIdAndStatusIn_returnsCorrectBookings() {
        List<Booking> bookings = bookingRepository.findAllByBookerIdAndStatusIn(
                booker.getId(), List.of(BookingStatus.WAITING), Sort.by("start").descending());
        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getStatus()).isEqualTo(BookingStatus.WAITING);
    }

    @Test
    void findAllByBookerIdAndStartLessThanAndEndGreaterThan_returnsBookings() {
        List<Booking> bookings = bookingRepository.findAllByBookerIdAndStartLessThanAndEndGreaterThan(
                booker.getId(), LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(1), Sort.by("start").descending());
        assertThat(bookings).isNotEmpty();
    }

    @Test
    void findAllByBookerIdAndEndLessThan_returnsPastBookings() {
        List<Booking> bookings = bookingRepository.findAllByBookerIdAndEndLessThan(
                booker.getId(), LocalDateTime.now().plusDays(3), Sort.by("start").descending());
        assertThat(bookings).isNotEmpty();
    }

    @Test
    void findAllByBookerIdAndStartGreaterThan_returnsFutureBookings() {
        List<Booking> bookings = bookingRepository.findAllByBookerIdAndStartGreaterThan(
                booker.getId(), LocalDateTime.now(), Sort.by("start").descending());
        assertThat(bookings).isNotEmpty();
    }

    @Test
    void findAllByItemIn_returnsBookings() {
        List<Booking> bookings = bookingRepository.findAllByItemIn(Collections.singletonList(item), Sort.by("start").descending());
        assertThat(bookings).isNotEmpty();
    }

    @Test
    void findAllByItemInAndStatusIn_returnsBookingsByStatus() {
        List<Booking> bookings = bookingRepository.findAllByItemInAndStatusIn(Collections.singletonList(item),
                List.of(BookingStatus.WAITING), Sort.by("start").descending());
        assertThat(bookings).isNotEmpty();
    }

    @Test
    void findAllByItemInAndStartLessThanAndEndGreaterThan_returnsBookings() {
        List<Booking> bookings = bookingRepository.findAllByItemInAndStartLessThanAndEndGreaterThan(
                Collections.singletonList(item), LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(1),
                Sort.by("start").descending());
        assertThat(bookings).isNotEmpty();
    }

    @Test
    void findAllByItemInAndEndLessThan_returnsPastBookings() {
        List<Booking> bookings = bookingRepository.findAllByItemInAndEndLessThan(
                Collections.singletonList(item), LocalDateTime.now().plusDays(3), Sort.by("start").descending());
        assertThat(bookings).isNotEmpty();
    }

    @Test
    void findAllByItemInAndStartGreaterThan_returnsFutureBookings() {
        List<Booking> bookings = bookingRepository.findAllByItemInAndStartGreaterThan(
                Collections.singletonList(item), LocalDateTime.now(), Sort.by("start").descending());
        assertThat(bookings).isNotEmpty();
    }

    @Test
    void findAllByItemId_returnsBookings() {
        List<Booking> bookings = bookingRepository.findAllByItemId(item.getId(), Sort.by("start").descending());
        assertThat(bookings).isNotEmpty();
    }

    @Test
    void findFirstByItemIdAndStatusAndEndBeforeOrderByEndDesc_returnsBooking() {
        Optional<Booking> optionalBooking = bookingRepository.findFirstByItemIdAndStatusAndEndBeforeOrderByEndDesc(
                item.getId(), BookingStatus.WAITING, LocalDateTime.now().plusDays(3));
        assertThat(optionalBooking).isPresent();
    }

    @Test
    void findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc_returnsBooking() {
        Optional<Booking> optionalBooking = bookingRepository.findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(
                item.getId(), BookingStatus.WAITING, LocalDateTime.now());
        assertThat(optionalBooking).isPresent();
    }

    @Test
    void existsByItemIdAndBookerIdAndStatusAndEndBefore_returnsTrue() {
        boolean exists = bookingRepository.existsByItemIdAndBookerIdAndStatusAndEndBefore(
                item.getId(), booker.getId(), BookingStatus.WAITING, LocalDateTime.now().plusDays(3));
        assertThat(exists).isTrue();
    }
}
