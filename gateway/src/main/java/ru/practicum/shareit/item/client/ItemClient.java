package ru.practicum.shareit.item.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

@Slf4j
@Component
public class ItemClient extends BaseClient {

    private static final String API_PREFIX = "/items";

    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory("http://shareit-server:9090" + API_PREFIX))
                        .build()
        );
        log.info("ItemClient initialized with server URL: {}", serverUrl + API_PREFIX);
    }

    public ResponseEntity<Object> create(ItemDto itemDto, Long userId, Long requestId) {
        String url = requestId != null ? "?requestId=" + requestId : "";
        return post(url, userId, itemDto);
    }

    public ResponseEntity<Object> update(Long itemId, ItemDto itemDto, Long userId) {
        return patch("/" + itemId, userId, itemDto);
    }

    public ResponseEntity<Object> findById(Long itemId, Long userId) {
        return get("/" + itemId, userId);
    }

    public ResponseEntity<Object> findAllByOwner(int from, int size, Long userId) {
        return get("?from=" + from + "&size=" + size, userId);
    }

    public ResponseEntity<Object> search(String text, int from, int size) {
        if (text == null || text.isBlank()) return ResponseEntity.ok().body(new Object[0]);
        return get("/search?text=" + text + "&from=" + from + "&size=" + size);
    }

    public ResponseEntity<Object> addComment(Long itemId, CommentDto commentDto, Long userId) {
        return post("/" + itemId + "/comment", userId, commentDto);
    }
}
