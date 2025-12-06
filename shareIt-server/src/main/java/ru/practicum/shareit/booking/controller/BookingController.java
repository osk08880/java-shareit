package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.State;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final BookingService bookingService;

    @PostMapping
    public BookingDto create(@RequestBody BookingDto bookingDto,
                             @RequestHeader(USER_ID_HEADER) Long userId) {
        return bookingService.create(bookingDto, userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approve(@PathVariable Long bookingId,
                              @RequestParam Boolean approved,
                              @RequestHeader(USER_ID_HEADER) Long userId) {
        return bookingService.approve(bookingId, userId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getById(@PathVariable Long bookingId,
                              @RequestHeader(USER_ID_HEADER) Long userId) {
        return bookingService.getById(bookingId, userId);
    }

    @GetMapping
    public List<BookingDto> getAll(@RequestParam(defaultValue = "ALL") State state,
                                   @RequestHeader(USER_ID_HEADER) Long userId) {
        return bookingService.getAll(state, userId);
    }

    @GetMapping("/owner")
    public List<BookingDto> getOwnerAll(@RequestParam(defaultValue = "ALL") State state,
                                        @RequestHeader(USER_ID_HEADER) Long ownerId) {
        return bookingService.getOwnerAll(state, ownerId);
    }
}