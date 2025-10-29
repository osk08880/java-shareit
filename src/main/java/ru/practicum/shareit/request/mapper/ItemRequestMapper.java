package ru.practicum.shareit.request.mapper;

import lombok.extern.slf4j.Slf4j;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@Slf4j
public class ItemRequestMapper {

    public static ItemRequestDto toItemRequestDto(ItemRequest request) {
        if (request == null) {
            log.warn("Попытка преобразовать null в ItemRequestDto");
            return null;
        }
        ItemRequestDto dto = ItemRequestDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .requestorId(request.getRequestor() != null ? request.getRequestor().getId() : null)
                .created(request.getCreated())
                .build();
        log.info("Преобразован ItemRequest в ItemRequestDto: {}", dto);
        return dto;
    }

    public static ItemRequest toItemRequest(ItemRequestDto dto, User requestor) {
        if (dto == null) {
            log.warn("Попытка преобразовать null в ItemRequest");
            return null;
        }
        ItemRequest request = ItemRequest.builder()
                .id(dto.getId())
                .description(dto.getDescription())
                .requestor(requestor)
                .created(dto.getCreated() != null ? dto.getCreated() : LocalDateTime.now())
                .build();
        log.info("Преобразован ItemRequestDto в ItemRequest: {}", request);
        return request;
    }
}
