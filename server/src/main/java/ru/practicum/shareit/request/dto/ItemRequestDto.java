package ru.practicum.shareit.request.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequestDto {

    private Long id;

    private String description;

    private UserDto requestor;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime created;

    private List<ItemDto> items;

    public static ItemRequestDto toDto(ItemRequest request, boolean includeItems) {
        if (request == null) return null;

        List<ItemDto> items = includeItems && request.getItems() != null ?
                request.getItems().stream()
                        .map(item -> ItemMapper.toItemDto(item, false))
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
