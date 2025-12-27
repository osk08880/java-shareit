package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findAllByBookerId(Long bookerId, Sort sort);

    List<Booking> findAllByBookerIdAndStatusIn(Long bookerId, List<BookingStatus> statuses, Sort sort);

    List<Booking> findAllByBookerIdAndStartLessThanAndEndGreaterThan(Long bookerId, LocalDateTime now, LocalDateTime nowEnd, Sort sort);

    List<Booking> findAllByBookerIdAndEndLessThan(Long bookerId, LocalDateTime now, Sort sort);

    List<Booking> findAllByBookerIdAndStartGreaterThan(Long bookerId, LocalDateTime now, Sort sort);

    List<Booking> findAllByItemIn(List<Item> items, Sort sort);

    List<Booking> findAllByItemInAndStatusIn(List<Item> items, List<BookingStatus> statuses, Sort sort);

    List<Booking> findAllByItemInAndStartLessThanAndEndGreaterThan(List<Item> items, LocalDateTime now, LocalDateTime nowEnd, Sort sort);

    List<Booking> findAllByItemInAndEndLessThan(List<Item> items, LocalDateTime now, Sort sort);

    List<Booking> findAllByItemInAndStartGreaterThan(List<Item> items, LocalDateTime now, Sort sort);

    List<Booking> findAllByItemId(Long itemId, Sort sort);

    Optional<Booking> findFirstByItemIdAndStatusAndEndBeforeOrderByEndDesc(Long itemId, BookingStatus status, LocalDateTime now);

    Optional<Booking> findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(Long itemId, BookingStatus status, LocalDateTime now);

    boolean existsByItemIdAndBookerIdAndStatusAndEndBefore(Long itemId, Long bookerId, BookingStatus status, LocalDateTime now);
}
