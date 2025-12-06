package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.State;

import java.util.List;

public interface BookingService {

    BookingDto create(BookingDto bookingDto, Long userId);

    BookingDto approve(Long bookingId, Long userId, Boolean approved);

    BookingDto getById(Long bookingId, Long userId);

    List<BookingDto> getAll(State state, Long userId);

    List<BookingDto> getOwnerAll(State state, Long userId);
}