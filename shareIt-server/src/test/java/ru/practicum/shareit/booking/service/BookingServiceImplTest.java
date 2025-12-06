package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.State;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User owner;
    private User booker;
    private Item item;
    private Booking booking;
    private BookingDto bookingDto;

    @BeforeEach
    void setUp() {
        owner = User.builder().id(1L).name("Alice").email("alice@mail.com").build();
        booker = User.builder().id(2L).name("Bob").email("bob@mail.com").build();
        item = Item.builder().id(10L).name("Drill").description("Cordless drill").available(true).owner(owner).build();

        booking = Booking.builder().id(100L).item(item).booker(booker)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.WAITING).build();

        bookingDto = BookingDto.builder().id(100L).itemId(item.getId()).bookerId(booker.getId())
                .start(booking.getStart()).end(booking.getEnd()).status(BookingStatus.WAITING).build();
    }

    @Test
    void createBookingFailUserNotFound() {
        when(userRepository.findById(booker.getId())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> bookingService.create(bookingDto, booker.getId()));
    }

    @Test
    void createBookingFailItemNotFound() {
        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> bookingService.create(bookingDto, booker.getId()));
    }

    @Test
    void approveBookingFailBookingNotFound() {
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> bookingService.approve(booking.getId(), owner.getId(), true));
    }

    @Test
    void getByIdFailBookingNotFound() {
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> bookingService.getById(booking.getId(), booker.getId()));
    }

    @Test
    void getAllFailUserNotFound() {
        when(userRepository.findById(booker.getId())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> bookingService.getAll(State.ALL, booker.getId()));
    }

    @Test
    void getOwnerAllFailUserNotFound() {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> bookingService.getOwnerAll(State.ALL, owner.getId()));
    }

    @Test
    void createBookingSuccess() {
        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(bookingMapper.toBooking(bookingDto, item, booker)).thenReturn(booking);
        when(bookingRepository.save(booking)).thenReturn(booking);
        when(bookingMapper.toBookingDto(booking)).thenReturn(bookingDto);

        BookingDto result = bookingService.create(bookingDto, booker.getId());

        assertNotNull(result);
        assertEquals(bookingDto.getId(), result.getId());
        verify(bookingRepository, times(1)).save(booking);
    }

    @Test
    void approveBookingSuccess() {
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);
        when(bookingMapper.toBookingDto(booking)).thenReturn(bookingDto);

        BookingDto result = bookingService.approve(booking.getId(), owner.getId(), true);

        assertEquals(BookingStatus.APPROVED, booking.getStatus());
        assertEquals(bookingDto.getId(), result.getId());
    }

    @Test
    void getByIdSuccess() {
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(bookingMapper.toBookingDto(booking)).thenReturn(bookingDto);

        BookingDto result = bookingService.getById(booking.getId(), booker.getId());
        assertEquals(bookingDto.getId(), result.getId());
    }
}
