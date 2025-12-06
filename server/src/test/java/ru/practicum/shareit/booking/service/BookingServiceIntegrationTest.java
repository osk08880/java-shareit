package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.State;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class BookingServiceIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private User owner;
    private User booker;
    private Item item;

    @BeforeEach
    void setUp() {
        owner = userRepository.save(User.builder().name("Alice").email("alice@example.com").build());
        booker = userRepository.save(User.builder().name("Bob").email("bob@example.com").build());

        item = itemRepository.save(Item.builder()
                .name("Drill")
                .description("Powerful drill")
                .available(true)
                .owner(owner)
                .build());

        createBooking(LocalDateTime.now().minusDays(5), LocalDateTime.now().minusDays(3), BookingStatus.APPROVED);
        createBooking(LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1), BookingStatus.APPROVED);
        createBooking(LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(4), BookingStatus.APPROVED);
        createBooking(LocalDateTime.now().plusDays(5), LocalDateTime.now().plusDays(6), BookingStatus.WAITING);
        createBooking(LocalDateTime.now().plusDays(7), LocalDateTime.now().plusDays(8), BookingStatus.REJECTED);
    }

    private void createBooking(LocalDateTime start, LocalDateTime end, BookingStatus status) {
        bookingRepository.save(Booking.builder()
                .item(item)
                .booker(booker)
                .status(status)
                .start(start)
                .end(end)
                .build());
    }

    @Test
    void createBooking_success() {
        BookingDto dto = BookingDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(10))
                .end(LocalDateTime.now().plusDays(12))
                .build();

        BookingDto created = bookingService.create(dto, booker.getId());

        assertNotNull(created.getId());
        assertEquals(item.getId(), created.getItemId());
        assertEquals(booker.getId(), created.getBookerId());
    }

    @Test
    void approveBooking_success() {
        Booking booking = bookingRepository.save(Booking.builder()
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build());

        BookingDto approved = bookingService.approve(booking.getId(), owner.getId(), true);
        assertEquals(BookingStatus.APPROVED, approved.getStatus());
    }

    @Test
    void getAllBookings_filters() {
        List<BookingDto> past = bookingService.getAll(State.PAST, booker.getId());
        assertEquals(1, past.size());
        assertTrue(past.stream().allMatch(b -> b.getEnd().isBefore(LocalDateTime.now())));

        List<BookingDto> current = bookingService.getAll(State.CURRENT, booker.getId());
        assertEquals(1, current.size());
        assertTrue(current.stream().allMatch(b -> !b.getStart().isAfter(LocalDateTime.now()) && !b.getEnd().isBefore(LocalDateTime.now())));

        List<BookingDto> future = bookingService.getAll(State.FUTURE, booker.getId());
        assertEquals(3, future.size());

        List<BookingDto> waiting = bookingService.getAll(State.WAITING, booker.getId());
        assertEquals(1, waiting.size());
        assertTrue(waiting.stream().allMatch(b -> b.getStatus() == BookingStatus.WAITING));

        List<BookingDto> rejected = bookingService.getAll(State.REJECTED, booker.getId());
        assertEquals(1, rejected.size());
        assertTrue(rejected.stream().allMatch(b -> b.getStatus() == BookingStatus.REJECTED));

        List<BookingDto> all = bookingService.getAll(State.ALL, booker.getId());
        assertEquals(5, all.size());
    }

    @Test
    void getOwnerAll_filters() {
        List<BookingDto> future = bookingService.getOwnerAll(State.FUTURE, owner.getId());
        assertEquals(3, future.size());

        List<BookingDto> waiting = bookingService.getOwnerAll(State.WAITING, owner.getId());
        assertEquals(1, waiting.size());

        List<BookingDto> rejected = bookingService.getOwnerAll(State.REJECTED, owner.getId());
        assertEquals(1, rejected.size());
    }

    @Test
    void getById_success() {
        Booking booking = bookingRepository.save(Booking.builder()
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build());

        BookingDto found = bookingService.getById(booking.getId(), booker.getId());

        assertNotNull(found);
        assertEquals(booking.getId(), found.getId());
        assertEquals(booker.getId(), found.getBookerId());
        assertEquals(item.getId(), found.getItemId());
    }

}
