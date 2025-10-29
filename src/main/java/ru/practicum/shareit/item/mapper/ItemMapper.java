package ru.practicum.shareit.item.mapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.request.model.ItemRequest;

@Slf4j
@Component
public class ItemMapper {

    public static ItemDto toItemDto(Item item) {
        if (item == null) {
            log.warn("Попытка преобразовать null Item в ItemDto");
            return null;
        }
        log.info("Преобразование Item в ItemDto: {}", item);
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .requestId(item.getRequest() != null ? item.getRequest().getId() : null)
                .build();
    }

    public static Item toItem(ItemDto itemDto, User owner, ItemRequest request) {
        if (itemDto == null) {
            log.warn("Попытка преобразовать null ItemDto в Item");
            return null;
        }
        log.info("Преобразование ItemDto в Item. ItemDto: {}, Владелец: {}, Запрос: {}", itemDto, owner, request);
        return Item.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .owner(owner)
                .request(request)
                .build();
    }
}
