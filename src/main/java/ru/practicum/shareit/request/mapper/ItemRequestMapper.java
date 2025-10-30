package ru.practicum.shareit.request.mapper;

import lombok.extern.slf4j.Slf4j;
import ru.practicum.shareit.exception.MappingException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

@Slf4j
public class ItemRequestMapper {

    public static ItemRequestDto toItemRequestDto(ItemRequest request) {
        if (request == null) {
            throw new MappingException("ItemRequest для преобразования в DTO не может быть null");
        }
        return ItemRequestDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .requestorId(request.getRequestor() != null ? request.getRequestor().getId() : null)
                .created(request.getCreated())
                .build();
    }

    public static ItemRequest toItemRequest(ItemRequestDto dto, User requestor) {
        if (dto == null) {
            throw new MappingException("ItemRequestDto для преобразования в ItemRequest не может быть null");
        }
        return ItemRequest.builder()
                .id(dto.getId())
                .description(dto.getDescription())
                .requestor(requestor)
                .created(dto.getCreated() != null ? dto.getCreated() : java.time.LocalDateTime.now())
                .build();
    }
}
