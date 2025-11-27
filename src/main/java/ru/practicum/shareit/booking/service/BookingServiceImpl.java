package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingMapper bookingMapper;

    @Override
    public BookingDto create(BookingDto dto, Long userId) {
        log.debug("Создание бронирования: userId={}, dto={}", userId, dto);

        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        Item item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        if (!Boolean.TRUE.equals(item.getAvailable())) {
            log.warn("Вещь {} недоступна для бронирования", item.getId());
            throw new IllegalArgumentException("Вещь недоступна для бронирования");
        }
        if (item.getOwnerId().equals(userId)) {
            log.warn("Пользователь {} пытается забронировать свою вещь {}", userId, item.getId());
            throw new NotFoundException("Нельзя бронировать свою вещь");
        }

        Booking booking = bookingMapper.toBooking(dto, item, booker);
        booking.setStatus(BookingStatus.WAITING);

        Booking saved = bookingRepository.save(booking);
        log.info("Бронирование создано: {}", saved);

        return bookingMapper.toBookingDto(saved);
    }

    @Override
    public BookingDto approve(Long bookingId, Long userId, Boolean approved) {
        log.debug("Подтверждение бронирования: bookingId={}, userId={}, approved={}", bookingId, userId, approved);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));
        Item item = booking.getItem();

        if (!item.getOwnerId().equals(userId)) {
            log.warn("Пользователь {} не владелец вещи {} и не может подтверждать бронирование", userId, item.getId());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Только владелец может подтверждать бронирование");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            log.warn("Бронирование {} не в статусе WAITING, текущее: {}", bookingId, booking.getStatus());
            throw new IllegalArgumentException("Статус бронирования не ожидает подтверждения");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking saved = bookingRepository.save(booking);

        log.info("Бронирование {} обновлено, новый статус: {}", bookingId, saved.getStatus());
        return bookingMapper.toBookingDto(saved);
    }

    @Override
    public BookingDto getById(Long bookingId, Long userId) {
        log.debug("Получение бронирования по ID: bookingId={}, userId={}", bookingId, userId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        if (!booking.getBooker().getId().equals(userId) && !booking.getItem().getOwnerId().equals(userId)) {
            log.warn("Пользователь {} не имеет доступа к бронированию {}", userId, bookingId);
            throw new NotFoundException("Нет доступа к бронированию");
        }

        log.info("Бронирование {} получено пользователем {}", bookingId, userId);
        return bookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> getAll(State state, Long userId) {
        log.debug("Получение всех бронирований пользователя {} с фильтром {}", userId, state);

        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings;

        switch (state) {
            case CURRENT:
                bookings = bookingRepository.findAllByBookerIdAndStartLessThanAndEndGreaterThan(userId, now, now, sort);
                break;
            case PAST:
                bookings = bookingRepository.findAllByBookerIdAndEndLessThan(userId, now, sort);
                break;
            case FUTURE:
                bookings = bookingRepository.findAllByBookerIdAndStartGreaterThan(userId, now, sort);
                break;
            case WAITING:
                bookings = bookingRepository.findAllByBookerIdAndStatusIn(userId, List.of(BookingStatus.WAITING), sort);
                break;
            case REJECTED:
                bookings = bookingRepository.findAllByBookerIdAndStatusIn(userId, List.of(BookingStatus.REJECTED), sort);
                break;
            default:
                bookings = bookingRepository.findAllByBookerId(userId, sort);
        }

        log.info("Найдено {} бронирований для пользователя {} с фильтром {}", bookings.size(), userId, state);
        return bookings.stream().map(bookingMapper::toBookingDto).collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getOwnerAll(State state, Long ownerId) {
        log.debug("Получение всех бронирований для вещей владельца {} с фильтром {}", ownerId, state);

        userRepository.findById(ownerId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        List<Item> items = itemRepository.findByOwnerId(ownerId);
        if (items.isEmpty()) {
            log.info("У пользователя {} нет вещей", ownerId);
            return Collections.emptyList();
        }

        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings;

        switch (state) {
            case CURRENT:
                bookings = bookingRepository.findAllByItemInAndStartLessThanAndEndGreaterThan(items, now, now, sort);
                break;
            case PAST:
                bookings = bookingRepository.findAllByItemInAndEndLessThan(items, now, sort);
                break;
            case FUTURE:
                bookings = bookingRepository.findAllByItemInAndStartGreaterThan(items, now, sort);
                break;
            case WAITING:
                bookings = bookingRepository.findAllByItemInAndStatusIn(items, List.of(BookingStatus.WAITING), sort);
                break;
            case REJECTED:
                bookings = bookingRepository.findAllByItemInAndStatusIn(items, List.of(BookingStatus.REJECTED), sort);
                break;
            default:
                bookings = bookingRepository.findAllByItemIn(items, sort);
        }

        log.info("Найдено {} бронирований для владельца {} с фильтром {}", bookings.size(), ownerId, state);
        return bookings.stream().map(bookingMapper::toBookingDto).collect(Collectors.toList());
    }
}
