package ru.practicum.shareit.request.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.Map;

@Slf4j
@Component
public class ItemRequestClient extends BaseClient {

    private static final String API_PREFIX = "/requests";

    public ItemRequestClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory("http://shareit-server:9090" + API_PREFIX))
                        .build()
        );
        log.info("ItemRequestClient initialized with server URL: {}", serverUrl + API_PREFIX);
    }

    public ResponseEntity<Object> create(Long userId, ItemRequestDto dto) {
        return post("", userId, dto);
    }

    public ResponseEntity<Object> getOwn(Long userId) {
        return get("", userId);
    }

    public ResponseEntity<Object> getAll(Long userId, Integer from, Integer size) {
        Map<String, Object> params = Map.of("from", from, "size", size);
        return get("/all?from={from}&size={size}", userId, params);
    }

    public ResponseEntity<Object> getById(Long userId, Long requestId) {
        return get("/" + requestId, userId);
    }
}
