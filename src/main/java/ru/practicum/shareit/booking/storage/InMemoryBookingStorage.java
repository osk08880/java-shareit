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
        return bookings.values().stream()
                .filter(b -> b.getItem() != null && b.getItem().getId().equals(itemId))
                .collect(Collectors.toList());
    }
}
