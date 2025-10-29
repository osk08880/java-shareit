package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.storage.InMemoryBookingStorage;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.storage.InMemoryItemStorage;
import ru.practicum.shareit.user.storage.InMemoryUserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final InMemoryBookingStorage bookingStorage;
    private final InMemoryItemStorage itemStorage;
    private final InMemoryUserStorage userStorage;

    @Override
    public BookingDto createBooking(BookingDto bookingDto) {
        userStorage.findById(bookingDto.getBookerId())
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        var item = itemStorage.findById(bookingDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Предмет не найден"));

        boolean conflict = bookingStorage.findByItemId(item.getId()).stream()
                .anyMatch(b -> b.getStatus() == BookingStatus.APPROVED &&
                        bookingDto.getStart().isBefore(b.getEnd()) &&
                        bookingDto.getEnd().isAfter(b.getStart()));
        if (conflict) {
            log.warn("Попытка создать пересекающееся бронирование для предмета {}", item.getId());
            throw new IllegalArgumentException("Время бронирования пересекается с существующим");
        }

        Booking booking = BookingMapper.toBooking(bookingDto, item, userStorage.findById(bookingDto.getBookerId()).get());
        Booking created = bookingStorage.create(booking);
        log.info("Бронирование создано: {}", created);
        return BookingMapper.toBookingDto(created);
    }

    @Override
    public List<BookingDto> findBookingsByItem(Long itemId) {
        return bookingStorage.findByItemId(itemId).stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }
}
