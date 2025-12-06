package ru.practicum.shareit.item.mapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingDtoForItem;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.Collections;

@Slf4j
@Component
public class ItemMapper {

    public static ItemDto toItemDto(Item item, boolean includeRequest) {
        if (item == null) return null;

        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .request(includeRequest && item.getRequest() != null ?
                        ItemRequestDto.toDto(item.getRequest(), false) : null)
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

    public static Item toItem(ItemDto itemDto, User owner, ru.practicum.shareit.request.model.ItemRequest request) {
        if (itemDto == null) throw new IllegalArgumentException("ItemDto не может быть null");
        return Item.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(Boolean.TRUE.equals(itemDto.getAvailable()))
                .owner(owner)
                .request(request)
                .build();
    }
}
