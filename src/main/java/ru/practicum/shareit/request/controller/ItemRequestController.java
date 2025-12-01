package ru.practicum.shareit.request.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
public class ItemRequestController {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDto create(@Valid @RequestBody ItemRequestDto dto,
                                 @RequestHeader(USER_ID_HEADER) Long userId) {
        return itemRequestService.create(dto, userId);
    }

    @GetMapping
    public List<ItemRequestDto> getOwn(@RequestHeader(USER_ID_HEADER) Long userId) {
        return itemRequestService.getOwnRequests(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAll(@RequestHeader(USER_ID_HEADER) Long userId) {
        return itemRequestService.getAllRequests(userId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getById(@PathVariable Long requestId,
                                  @RequestHeader(USER_ID_HEADER) Long userId) {
        return itemRequestService.getRequestById(requestId, userId);
    }
}