package ru.practicum.shareit.booking.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.model.Booking;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class InMemoryBookingStorage {
    private final Map<Long, Booking> bookings = new HashMap<>();
    private long idCounter = 1;

    public Booking create(Booking booking) {
        booking.setId(idCounter++);
        bookings.put(booking.getId(), booking);
        log.info("Бронирование добавлено: {}", booking);
        return booking;
    }

    public List<Booking> findByItemId(Long itemId) {
        if (itemId == null) {
            log.warn("Поиск бронирований по itemId = null — возвращен пустой список");
            return Collections.emptyList();
        }

        return bookings.values().stream()
                .filter(Objects::nonNull)
                .filter(booking -> booking.getItem() != null)
                .filter(booking -> itemId.equals(booking.getItem().getId()))
                .collect(Collectors.toList());
    }
}
