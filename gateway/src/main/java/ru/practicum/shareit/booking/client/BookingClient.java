package ru.practicum.shareit.booking.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.State;
import ru.practicum.shareit.client.BaseClient;

@Slf4j
@Component
public class BookingClient extends BaseClient {

    private static final String API_PREFIX = "/bookings";

    public BookingClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory("http://shareit-server:9090" + API_PREFIX))
                        .build()
        );
        log.info("BookingClient initialized with server URL: {}", serverUrl + API_PREFIX);
    }

    public ResponseEntity<Object> create(BookingDto bookingDto, Long userId) {
        return post("", userId, bookingDto);
    }

    public ResponseEntity<Object> approve(Long bookingId, Boolean approved, Long userId) {
        String url = "/" + bookingId + "?approved=" + approved;
        return patch(url, userId, null);
    }

    public ResponseEntity<Object> getById(Long bookingId, Long userId) {
        return get("/" + bookingId, userId);
    }

    public ResponseEntity<Object> getAll(State state, Long userId) {
        String url = "?state=" + state.name();
        return get(url, userId);
    }

    public ResponseEntity<Object> getOwnerAll(State state, Long userId) {
        String url = "/owner?state=" + state.name();
        return get(url, userId);
    }
}
