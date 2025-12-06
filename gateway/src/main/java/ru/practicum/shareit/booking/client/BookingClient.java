package ru.practicum.shareit.booking.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.State;
import ru.practicum.shareit.client.BaseClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class BookingClient extends BaseClient {

    private static final String BASE_PATH = "/bookings";
    private static final String HEADER_USER_ID = "X-Sharer-User-Id";
    private static final String OWNER_PATH = "/owner";

    public BookingClient(RestTemplateBuilder builder) {
        super(builder.build());
    }

    public ResponseEntity<Object> create(BookingDto bookingDto, Long userId) {
        return post(BASE_PATH, bookingDto, Map.of(), Object.class, HEADER_USER_ID, userId);
    }

    public ResponseEntity<Object> approve(Long bookingId, Boolean approved, Long userId) {
        String url = BASE_PATH + "/{bookingId}?approved={approved}";
        return patch(url, null, Map.of("bookingId", bookingId, "approved", approved), Object.class, HEADER_USER_ID, userId);
    }

    public ResponseEntity<Object> getById(Long bookingId, Long userId) {
        String url = BASE_PATH + "/{bookingId}";
        return get(url, Map.of("bookingId", bookingId), Object.class, HEADER_USER_ID, userId);
    }

    public ResponseEntity<List<BookingDto>> getAll(State state, Long userId) {
        String url = BASE_PATH + "?state={state}";
        return get(url, Map.of("state", state.name()), new ParameterizedTypeReference<>() {}, HEADER_USER_ID, userId);
    }

    public ResponseEntity<List<BookingDto>> getOwnerAll(State state, Long userId) {
        String url = BASE_PATH + OWNER_PATH + "?state={state}";
        return get(url, Map.of("state", state.name()), new ParameterizedTypeReference<>() {}, HEADER_USER_ID, userId);
    }
}
