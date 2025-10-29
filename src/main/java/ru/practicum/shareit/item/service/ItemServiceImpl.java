package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.InMemoryItemHistoryStorage;
import ru.practicum.shareit.item.storage.InMemoryItemStorage;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.InMemoryItemRequestStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.InMemoryUserStorage;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final InMemoryItemStorage itemStorage;
    private final InMemoryUserStorage userStorage;
    private final InMemoryItemRequestStorage requestStorage;
    private final InMemoryItemHistoryStorage historyStorage;

    @Override
    public ItemDto create(Long userId, ItemDto itemDto, Long requestId) {
        User owner = userStorage.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь с ID {} не найден при попытке создать вещь", userId);
                    return new NotFoundException("Пользователь не найден");
                });

        ItemRequest request = (requestId != null) ? requestStorage.findById(requestId)
                .orElseThrow(() -> {
                    log.warn("Запрос с ID {} не найден при создании вещи", requestId);
                    return new NotFoundException("Запрос не найден");
                }) : null;

        Item item = ItemMapper.toItem(itemDto, owner, request);
        Item createdItem = itemStorage.create(item);
        log.info("Вещь создана пользователем {}: {}", userId, createdItem);
        return ItemMapper.toItemDto(createdItem);
    }

    @Override
    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        Item existingItem = itemStorage.findById(itemId)
                .orElseThrow(() -> {
                    log.warn("Вещь с ID {} не найдена при попытке обновления", itemId);
                    return new NotFoundException("Вещь не найдена");
                });

        if (!existingItem.getOwner().getId().equals(userId)) {
            log.warn("Пользователь {} попытался обновить вещь {} без прав", userId, itemId);
            throw new NotFoundException("Только владелец может обновить вещь");
        }

        Item updatedItem = Item.builder()
                .id(itemId)
                .name(itemDto.getName() != null ? itemDto.getName().trim() : existingItem.getName())
                .description(itemDto.getDescription() != null ? itemDto.getDescription().trim() : existingItem.getDescription())
                .available(itemDto.getAvailable() != null ? itemDto.getAvailable() : existingItem.getAvailable())
                .owner(existingItem.getOwner())
                .request(existingItem.getRequest())
                .build();

        itemStorage.update(updatedItem)
                .orElseThrow(() -> {
                    log.warn("Не удалось обновить вещь с ID {}", itemId);
                    return new NotFoundException("Обновление не удалось");
                });

        log.info("Вещь обновлена пользователем {}: {}", userId, updatedItem);
        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    public ItemDto findById(Long itemId) {
        Item item = itemStorage.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Предмет с ID " + itemId + " не найден"));
        log.info("Предмет найден: {}", item);

        historyStorage.addView(item.getOwner().getId(), itemId);

        return ItemMapper.toItemDto(item);
    }

    public List<ItemDto> getHistoryForUser(Long userId) {
        List<Long> itemIds = historyStorage.getHistory(userId);
        return itemIds.stream()
                .map(itemStorage::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    public List<ItemDto> findAllByOwner(Long userId) {
        User owner = userStorage.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь с ID {} не найден при получении списка своих вещей", userId);
                    return new NotFoundException("Пользователь не найден");
                });
        List<ItemDto> items = itemStorage.findAllByOwner(userId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
        log.info("Найдено {} вещей для пользователя {}", items.size(), userId);
        return items;
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            log.info("Пустой текст поиска, возвращен пустой список");
            return Collections.emptyList();
        }
        List<ItemDto> result = itemStorage.search(text).stream()
                .filter(Item::getAvailable)
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
        log.info("Поиск '{}' вернул {} доступных вещей", text, result.size());
        return result;
    }
}
