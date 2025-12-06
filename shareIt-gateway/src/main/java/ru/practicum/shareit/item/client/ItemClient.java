package ru.practicum.shareit.item.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.client.BaseClient;

import java.util.HashMap;

@Slf4j
@Component
public class ItemClient extends BaseClient {

    private static final String HEADER_USER_ID = "X-Sharer-User-Id";

    public ItemClient(RestTemplateBuilder builder) {
        super(builder.build());
    }

    public ResponseEntity<Object> create(ItemDto itemDto, Long userId, Long requestId) {
        String url = "/items";
        if (requestId != null) url += "?requestId=" + requestId;
        return post(url, itemDto, new HashMap<>(), Object.class, HEADER_USER_ID, userId);
    }

    public ResponseEntity<Object> update(Long itemId, ItemDto itemDto, Long userId) {
        return patch("/items/" + itemId, itemDto, new HashMap<>(), Object.class, HEADER_USER_ID, userId);
    }

    public ResponseEntity<Object> findById(Long itemId, Long userId) {
        return get("/items/" + itemId, new HashMap<>(), Object.class, HEADER_USER_ID, userId);
    }

    public ResponseEntity<Object> findAllByOwner(int from, int size, Long userId) {
        String url = "/items?from=" + from + "&size=" + size;
        return get(url, new HashMap<>(), Object.class, HEADER_USER_ID, userId);
    }

    public ResponseEntity<Object> search(String text, int from, int size) {
        if (text == null || text.isBlank()) return ResponseEntity.ok().body(new Object[0]);
        String url = "/items/search?text=" + text + "&from=" + from + "&size=" + size;
        return get(url, new HashMap<>(), Object.class, null, null);
    }

    public ResponseEntity<Object> addComment(Long itemId, CommentDto commentDto, Long userId) {
        return post("/items/" + itemId + "/comment", commentDto, new HashMap<>(), Object.class, HEADER_USER_ID, userId);
    }
}
