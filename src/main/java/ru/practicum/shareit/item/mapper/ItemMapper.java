package ru.practicum.shareit.item.mapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.MappingException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

@Slf4j
@Component
public class ItemMapper {

    public static ItemDto toItemDto(Item item) {
        if (item == null) {
            log.warn("Попытка преобразовать null Item в ItemDto");
            throw new MappingException("Item для преобразования в ItemDto не может быть null");
        }

        log.debug("Начало маппинга Item в ItemDto: {}", item);

        ItemDto dto = ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .request(item.getRequest())
                .build();

        log.debug("Завершен маппинг Item в ItemDto: {}", dto);
        return dto;
    }

    public static Item toItem(ItemDto itemDto, User owner, ItemRequest request) {
        if (itemDto == null) {
            log.warn("Попытка преобразовать null ItemDto в Item");
            throw new MappingException("ItemDto для преобразования в Item не может быть null");
        }

        log.debug("Начало маппинга ItemDto в Item: dto={}, owner={}, request={}", itemDto, owner, request);

        Item item = Item.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(Boolean.TRUE.equals(itemDto.getAvailable()))
                .owner(owner)
                .request(request)
                .build();

        log.debug("Завершен маппинг ItemDto в Item: {}", item);
        return item;
    }
}