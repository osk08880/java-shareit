package ru.practicum.shareit.item.mapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.MappingException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

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
                .requestId(item.getRequestId())
                .build();

        log.debug("Завершен маппинг Item в ItemDto: {}", dto);
        return dto;
    }

    public static Item toItem(ItemDto itemDto, Long ownerId, Long requestId) {
        if (itemDto == null) {
            log.warn("Попытка преобразовать null ItemDto в Item");
            throw new MappingException("ItemDto для преобразования в Item не может быть null");
        }

        log.debug("Начало маппинга ItemDto в Item: dto={}, ownerId={}, requestId={}", itemDto, ownerId, requestId);

        Item item = Item.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(Boolean.TRUE.equals(itemDto.getAvailable()))
                .ownerId(ownerId)
                .requestId(requestId)
                .build();

        log.debug("Завершен маппинг ItemDto в Item: {}", item);
        return item;
    }
}
