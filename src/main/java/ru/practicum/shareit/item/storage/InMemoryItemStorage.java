package ru.practicum.shareit.item.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class InMemoryItemStorage {
    private final Map<Long, Item> items = new HashMap<>();
    private long idCounter = 1;

    public Item create(Item item) {
        item.setId(idCounter++);
        items.put(item.getId(), item);
        log.info("Вещь создана: {}", item);
        return item;
    }

    public Optional<Item> update(Item item) {
        if (!items.containsKey(item.getId())) {
            log.warn("Попытка обновить несуществующую вещь с ID {}", item.getId());
            return Optional.empty();
        }
        items.put(item.getId(), item);
        log.info("Вещь обновлена: {}", item);
        return Optional.of(item);
    }

    public Optional<Item> findById(Long itemId) {
        Item item = items.get(itemId);
        if (item == null) {
            log.warn("Вещь с ID {} не найдена", itemId);
            return Optional.empty();
        }
        log.info("Вещь найдена по ID {}: {}", itemId, item);
        return Optional.of(item);
    }

    public List<Item> findAllByOwner(Long ownerId) {
        List<Item> ownedItems = items.values().stream()
                .filter(i -> i.getOwner().getId().equals(ownerId))
                .collect(Collectors.toList());
        log.info("Найдено {} вещей для владельца с ID {}", ownedItems.size(), ownerId);
        return ownedItems;
    }

    public List<Item> search(String text) {
        List<Item> result = items.values().stream()
                .filter(i -> i.getName().toLowerCase().contains(text.toLowerCase())
                        || i.getDescription().toLowerCase().contains(text.toLowerCase()))
                .collect(Collectors.toList());
        log.info("Поиск '{}' вернул {} вещей", text, result.size());
        return result;
    }
}
