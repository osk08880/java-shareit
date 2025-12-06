package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingDtoForItem;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collections;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {

    private Long id;

    private String name;

    private String description;

    @NotNull
    private Boolean available;

    private ItemRequestDto request;

    private UserDto owner;

    private Long requestId;

    private BookingDtoForItem lastBooking;

    private BookingDtoForItem nextBooking;

    private List<CommentDto> comments;

    public static ItemDto toItemDto(Item item) {
        if (item == null) return null;
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .request(item.getRequest() != null ? ItemRequestDto.toDto(item.getRequest(), true) : null)
                .owner(item.getOwner() != null ? UserDto.builder()
                        .id(item.getOwner().getId())
                        .name(item.getOwner().getName())
                        .email(item.getOwner().getEmail())
                        .build() : null)
                .lastBooking(item.getLastBooking() != null ? BookingDtoForItem.toDto(item.getLastBooking()) : null)
                .nextBooking(item.getNextBooking() != null ? BookingDtoForItem.toDto(item.getNextBooking()) : null)
                .comments(Collections.emptyList())
                .build();
    }

}