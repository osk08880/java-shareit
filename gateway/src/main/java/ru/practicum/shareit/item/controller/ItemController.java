package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.client.ItemClient;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

@Slf4j
@RestController
@RequestMapping(ItemController.BASE_PATH)
@RequiredArgsConstructor
public class ItemController {

    private final ItemClient itemClient;

    public static final String BASE_PATH = "/items";

    private static final String HEADER_USER_ID = "X-Sharer-User-Id";
    private static final String COMMENT_PATH = "/{itemId}/comment";
    private static final String SEARCH_PATH = "/search";

    private static final int DEFAULT_FROM = 0;
    private static final int DEFAULT_SIZE = 10;

    @PostMapping
    public ResponseEntity<Object> create(
            @RequestHeader(HEADER_USER_ID) Long userId,
            @RequestBody ItemDto itemDto,
            @RequestParam(required = false) Long requestId) {

        log.info("GATEWAY: POST {} userId={} {}", BASE_PATH, userId, itemDto);
        return itemClient.create(itemDto, userId, requestId);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(
            @PathVariable Long itemId,
            @RequestHeader(HEADER_USER_ID) Long userId,
            @RequestBody ItemDto itemDto) {

        log.info("GATEWAY: PATCH {}/{} userId={}", BASE_PATH, itemId, userId);
        return itemClient.update(itemId, itemDto, userId);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> findById(
            @PathVariable Long itemId,
            @RequestHeader(HEADER_USER_ID) Long userId) {

        log.info("GATEWAY: GET {}/{} userId={}", BASE_PATH, itemId, userId);
        return itemClient.findById(itemId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> findAllByOwner(
            @RequestParam(defaultValue = "" + DEFAULT_FROM) int from,
            @RequestParam(defaultValue = "" + DEFAULT_SIZE) int size,
            @RequestHeader(HEADER_USER_ID) Long userId) {

        log.info("GATEWAY: GET {}?from={}&size={} userId={}", BASE_PATH, from, size, userId);
        return itemClient.findAllByOwner(from, size, userId);
    }

    @GetMapping(SEARCH_PATH)
    public ResponseEntity<Object> search(
            @RequestParam String text,
            @RequestParam(defaultValue = "" + DEFAULT_FROM) int from,
            @RequestParam(defaultValue = "" + DEFAULT_SIZE) int size) {

        log.info("GATEWAY: GET {}{}?text={}&from={}&size={}", BASE_PATH, SEARCH_PATH, text, from, size);
        return itemClient.search(text, from, size);
    }

    @PostMapping(COMMENT_PATH)
    public ResponseEntity<Object> addComment(
            @PathVariable Long itemId,
            @RequestHeader(HEADER_USER_ID) Long userId,
            @RequestBody CommentDto commentDto) {

        log.info("GATEWAY: POST {}/{} userId={} {}", BASE_PATH, itemId, userId, commentDto);
        return itemClient.addComment(itemId, commentDto, userId);
    }
}
