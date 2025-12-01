package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/items")
@RequiredArgsConstructor
public class ItemController {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final ItemService itemService;

    @PostMapping
    public ItemDto create(@Valid @RequestBody ItemDto itemDto,
                          @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.create(userId, itemDto, itemDto.getRequestId());
    }

    @GetMapping("/{itemId}")
    public ItemDto findById(@RequestHeader(USER_ID_HEADER) Long userId,
                            @PathVariable Long itemId) {
        log.info("Запрос GET /items/{} - получение вещи пользователем {}", itemId, userId);
        return itemService.findById(userId, itemId);
    }

    @GetMapping
    public List<ItemDto> findAllByOwner(@RequestHeader(USER_ID_HEADER) Long userId,
                                        @RequestParam(defaultValue = "0") int from,
                                        @RequestParam(defaultValue = "10") int size) {
        log.info("Запрос GET /items - получение всех вещей пользователя {}", userId);
        return itemService.findAllByOwner(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam String text) {
        log.info("Запрос GET /items/search?text={} - поиск вещей", text);
        return itemService.search(text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@PathVariable Long itemId,
                                 @Valid @RequestBody CommentDto commentDto,
                                 @RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Запрос POST /items/{}/comment - добавление отзыва пользователем {}", itemId, userId);
        return itemService.addComment(commentDto, userId, itemId);
    }
}