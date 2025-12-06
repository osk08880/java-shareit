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

    private static final String MSG_USER_NOT_FOUND = "Пользователь не найден: ";
    private static final String MSG_ITEM_NOT_FOUND = "Вещь не найдена: ";
    private static final String MSG_BOOKING_NOT_FOUND = "Бронирование не найдено: ";
    private static final String MSG_NO_ACCESS = "Нет доступа к бронированию: ";
    private static final String MSG_ITEM_NOT_AVAILABLE = "Вещь недоступна для бронирования";
    private static final String MSG_CANNOT_BOOK_OWN_ITEM = "Нельзя бронировать свою вещь";
    private static final String MSG_ONLY_OWNER_CAN_APPROVE = "Только владелец может подтверждать бронирование";
    private static final String MSG_WRONG_STATUS = "Статус бронирования не ожидает подтверждения";

    @Override
    public BookingDto create(BookingDto dto, Long userId) {
        log.info("SERVER: Создание бронирования: userId={}, itemId={}", userId, dto.getItemId());

        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(MSG_USER_NOT_FOUND + userId));
        Item item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new NotFoundException(MSG_ITEM_NOT_FOUND + dto.getItemId()));

        log.debug("Item found: ID={}, available={}", item.getId(), item.getAvailable());

        if (!Boolean.TRUE.equals(item.getAvailable())) {
            log.warn(MSG_ITEM_NOT_AVAILABLE);
            throw new IllegalArgumentException(MSG_ITEM_NOT_AVAILABLE);
        }

        if (item.getOwner().getId().equals(userId)) {
            log.warn(MSG_CANNOT_BOOK_OWN_ITEM);
            throw new NotFoundException(MSG_CANNOT_BOOK_OWN_ITEM);
        }

        Booking booking = bookingMapper.toBooking(dto, item, booker);
        booking.setStatus(BookingStatus.WAITING);

        Booking saved = bookingRepository.save(booking);
        log.info("SERVER: Бронирование создано: {}", saved);

        return bookingMapper.toBookingDto(saved);
    }

    @Override
    public BookingDto approve(Long bookingId, Long userId, Boolean approved) {
        log.info("SERVER: Подтверждение бронирования: bookingId={}, userId={}, approved={}", bookingId, userId, approved);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(MSG_BOOKING_NOT_FOUND + bookingId));
        Item item = booking.getItem();

        if (!item.getOwner().getId().equals(userId)) {
            log.warn(MSG_ONLY_OWNER_CAN_APPROVE);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, MSG_ONLY_OWNER_CAN_APPROVE);
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            log.warn(MSG_WRONG_STATUS + ": {}", booking.getStatus());
            throw new IllegalArgumentException(MSG_WRONG_STATUS);
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking saved = bookingRepository.save(booking);

        log.info("SERVER: Бронирование {} обновлено, новый статус: {}", bookingId, saved.getStatus());
        return bookingMapper.toBookingDto(saved);
    }

    @Override
    public BookingDto getById(Long bookingId, Long userId) {
        log.info("SERVER: Получение бронирования по ID: bookingId={}, userId={}", bookingId, userId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(MSG_BOOKING_NOT_FOUND + bookingId));

        if (!booking.getBooker().getId().equals(userId) && !booking.getItem().getOwner().getId().equals(userId)) {
            log.warn(MSG_NO_ACCESS);
            throw new NotFoundException(MSG_NO_ACCESS + bookingId);
        }

        log.info("SERVER: Бронирование {} получено пользователем {}", bookingId, userId);
        return bookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> getAll(State state, Long userId) {
        log.info("SERVER: Получение бронирований пользователя {} с фильтром {}", userId, state);

        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(MSG_USER_NOT_FOUND + userId));

        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings;

        switch (state) {
            case CURRENT -> bookings = bookingRepository.findAllByBookerIdAndStartLessThanAndEndGreaterThan(userId, now, now, sort);
            case PAST -> bookings = bookingRepository.findAllByBookerIdAndEndLessThan(userId, now, sort);
            case FUTURE -> bookings = bookingRepository.findAllByBookerIdAndStartGreaterThan(userId, now, sort);
            case WAITING -> bookings = bookingRepository.findAllByBookerIdAndStatusIn(userId, List.of(BookingStatus.WAITING), sort);
            case REJECTED -> bookings = bookingRepository.findAllByBookerIdAndStatusIn(userId, List.of(BookingStatus.REJECTED), sort);
            default -> bookings = bookingRepository.findAllByBookerId(userId, sort);
        }

        log.info("SERVER: Найдено {} бронирований для пользователя {} с фильтром {}", bookings.size(), userId, state);
        return bookings.stream().map(bookingMapper::toBookingDto).collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getOwnerAll(State state, Long ownerId) {
        log.info("SERVER: Получение бронирований для вещей владельца {} с фильтром {}", ownerId, state);

        userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException(MSG_USER_NOT_FOUND + ownerId));

        List<Item> items = itemRepository.findByOwnerId(ownerId);
        if (items.isEmpty()) {
            log.info("SERVER: У пользователя {} нет вещей", ownerId);
            return Collections.emptyList();
        }

        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings;

        switch (state) {
            case CURRENT -> bookings = bookingRepository.findAllByItemInAndStartLessThanAndEndGreaterThan(items, now, now, sort);
            case PAST -> bookings = bookingRepository.findAllByItemInAndEndLessThan(items, now, sort);
            case FUTURE -> bookings = bookingRepository.findAllByItemInAndStartGreaterThan(items, now, sort);
            case WAITING -> bookings = bookingRepository.findAllByItemInAndStatusIn(items, List.of(BookingStatus.WAITING), sort);
            case REJECTED -> bookings = bookingRepository.findAllByItemInAndStatusIn(items, List.of(BookingStatus.REJECTED), sort);
            default -> bookings = bookingRepository.findAllByItemIn(items, sort);
        }

        log.info("SERVER: Найдено {} бронирований для владельца {} с фильтром {}", bookings.size(), ownerId, state);
        return bookings.stream().map(bookingMapper::toBookingDto).collect(Collectors.toList());
    }
}
