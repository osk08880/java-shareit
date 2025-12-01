package ru.practicum.shareit.request.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.model.Item;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequestResponseDto {
    private Long id;
    private String name;
    private Long ownerId;

    public static ItemRequestResponseDto toDto(Item item) {
        return ItemRequestResponseDto.builder()
                .id(item.getId())
                .name(item.getName())
                .ownerId(item.getOwner().getId())
                .build();
    }
}