package ru.practicum.shareit.request.mapper;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.mapper.UserMapper;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ItemRequestMapper {

    public static ItemRequest toItemRequest(ItemRequestDto dto, ru.practicum.shareit.user.model.User requestor) {
        if (dto == null) throw new IllegalArgumentException("ItemRequestDto не может быть null");
        return ItemRequest.builder()
                .id(dto.getId())
                .description(dto.getDescription())
                .requestor(requestor)
                .created(dto.getCreated() != null ? dto.getCreated() : java.time.LocalDateTime.now())
                .build();
    }

    public static ItemRequestDto toItemRequestDto(ItemRequest request) {
        return toDto(request, true);
    }

    public static ItemRequestDto toDto(ItemRequest request, boolean includeItems) {
        if (request == null) return null;

        List<ItemDto> items = includeItems
                ? Objects.requireNonNullElse(request.getItems(), Collections.emptyList()).stream()
                .map(item -> ItemMapper.toItemDto((ru.practicum.shareit.item.model.Item) item, false))
                .collect(Collectors.toList())
                : Collections.emptyList();

        return ItemRequestDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .requestor(UserMapper.toUserDto(request.getRequestor()))
                .created(request.getCreated())
                .items(items)
                .build();
    }
}
