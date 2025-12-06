package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.client.ItemRequestClient;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(ItemRequestController.BASE_PATH)
@RequiredArgsConstructor
public class ItemRequestController {

    private final ItemRequestClient requestClient;

    public static final String BASE_PATH = "/requests";

    private static final String HEADER_USER_ID = "X-Sharer-User-Id";
    private static final String ALL_PATH = "/all";
    private static final String PATH_BY_ID = "/{requestId}";

    private static final int DEFAULT_FROM = 0;
    private static final int DEFAULT_SIZE = 20;

    @PostMapping
    public ResponseEntity<Object> create(
            @RequestHeader(HEADER_USER_ID) Long userId,
            @RequestBody ItemRequestDto dto) {

        log.info("GATEWAY: POST {} userId={} {}", BASE_PATH, userId, dto);
        return requestClient.create(userId, dto);
    }

    @GetMapping
    public ResponseEntity<List<ItemRequestDto>> getOwn(
            @RequestHeader(HEADER_USER_ID) Long userId) {

        log.info("GATEWAY: GET {} userId={}", BASE_PATH, userId);
        return requestClient.getOwn(userId);
    }

    @GetMapping(ALL_PATH)
    public ResponseEntity<List<ItemRequestDto>> getAll(
            @RequestHeader(HEADER_USER_ID) Long userId,
            @RequestParam(defaultValue = "" + DEFAULT_FROM) Integer from,
            @RequestParam(defaultValue = "" + DEFAULT_SIZE) Integer size) {

        log.info("GATEWAY: GET {}{} userId={}, from={}, size={}", BASE_PATH, ALL_PATH, userId, from, size);
        return requestClient.getAll(userId, from, size);
    }

    @GetMapping(PATH_BY_ID)
    public ResponseEntity<Object> getById(
            @RequestHeader(HEADER_USER_ID) Long userId,
            @PathVariable Long requestId) {

        log.info("GATEWAY: GET {}/{} userId={}", BASE_PATH, requestId, userId);
        return requestClient.getById(userId, requestId);
    }
}
