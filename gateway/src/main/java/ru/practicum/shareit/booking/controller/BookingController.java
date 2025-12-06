package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.client.BookingClient;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.State;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(BookingController.BASE_PATH)
@RequiredArgsConstructor
public class BookingController {

    private final BookingClient bookingClient;

    public static final String BASE_PATH = "/bookings";

    private static final String HEADER_USER_ID = "X-Sharer-User-Id";
    private static final String OWNER_PATH = "/owner";
    private static final String DEFAULT_STATE = "ALL";

    private static final String LOG_PREFIX = "GATEWAY: {} {} userId={} {}";

    @PostMapping
    public ResponseEntity<Object> create(
            @RequestHeader(HEADER_USER_ID) Long userId,
            @RequestBody BookingDto bookingDto) {

        log.info(LOG_PREFIX, "POST", BASE_PATH, userId, bookingDto);
        return bookingClient.create(bookingDto, userId);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> approve(
            @PathVariable Long bookingId,
            @RequestParam Boolean approved,
            @RequestHeader(HEADER_USER_ID) Long userId) {

        log.info("GATEWAY: PATCH {}/{}?approved={} userId={}", BASE_PATH, bookingId, approved, userId);
        return bookingClient.approve(bookingId, approved, userId);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getById(
            @PathVariable Long bookingId,
            @RequestHeader(HEADER_USER_ID) Long userId) {

        log.info("GATEWAY: GET {}/{} userId={}", BASE_PATH, bookingId, userId);
        return bookingClient.getById(bookingId, userId);
    }

    @GetMapping
    public ResponseEntity<List<BookingDto>> getAll(
            @RequestParam(defaultValue = DEFAULT_STATE) State state,
            @RequestHeader(HEADER_USER_ID) Long userId) {

        log.info("GATEWAY: GET {}?state={} userId={}", BASE_PATH, state, userId);
        return bookingClient.getAll(state, userId);
    }

    @GetMapping(OWNER_PATH)
    public ResponseEntity<List<BookingDto>> getOwnerAll(
            @RequestParam(defaultValue = DEFAULT_STATE) State state,
            @RequestHeader(HEADER_USER_ID) Long userId) {

        log.info("GATEWAY: GET {}{}?state={} userId={}", BASE_PATH, OWNER_PATH, state, userId);
        return bookingClient.getOwnerAll(state, userId);
    }
}
