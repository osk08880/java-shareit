package ru.practicum.shareit.request.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.client.BaseClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ItemRequestClient extends BaseClient {

    private static final String BASE_PATH = "/requests";
    private static final String ALL_PATH = "/all";
    private static final String PATH_BY_ID = "/{requestId}";
    private static final String HEADER_USER_ID = "X-Sharer-User-Id";

    public ItemRequestClient(RestTemplateBuilder builder) {
        super(builder.build());
    }

    public ResponseEntity<Object> create(Long userId, ItemRequestDto dto) {
        return post(BASE_PATH, dto, Map.of(), Object.class, HEADER_USER_ID, userId);
    }

    public ResponseEntity<List<ItemRequestDto>> getOwn(Long userId) {
        return get(BASE_PATH, Map.of(), new ParameterizedTypeReference<>() {}, HEADER_USER_ID, userId);
    }

    public ResponseEntity<List<ItemRequestDto>> getAll(Long userId, Integer from, Integer size) {
        String url = BASE_PATH + ALL_PATH + "?from={from}&size={size}";
        return get(url, Map.of("from", from, "size", size), new ParameterizedTypeReference<>() {}, HEADER_USER_ID, userId);
    }

    public ResponseEntity<Object> getById(Long userId, Long requestId) {
        return get(BASE_PATH + PATH_BY_ID, Map.of("requestId", requestId), Object.class, HEADER_USER_ID, userId);
    }
}
