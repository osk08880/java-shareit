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

    private String normalize(String s) {
        return s == null ? "" : s.toLowerCase();
    }

    private boolean matches(Item item, String query) {
        String name = normalize(item.getName());
        String description = normalize(item.getDescription());
        return name.contains(query) || description.contains(query);
    }

    public Item create(Item item) {
        item.setId(idCounter++);
        items.put(item.getId(), item);
        log.info("Item создан с ID: {}", item.getId());
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
        return Optional.ofNullable(items.get(itemId))
                .map(item -> {
                    log.info("Вещь найдена по ID {}: {}", itemId, item);
                    return item;
                })
                .or(() -> {
                    log.warn("Вещь с ID {} не найдена", itemId);
                    return Optional.empty();
                });
    }

    public List<Item> findAllByOwner(Long ownerId) {
        if (ownerId == null) {
            log.warn("Поиск вещей по ownerId = null — возвращен пустой список");
            return Collections.emptyList();
        }

        List<Item> ownedItems = items.values().stream()
                .filter(Objects::nonNull)
                .filter(item -> ownerId.equals(item.getOwner().getId()))
                .collect(Collectors.toList());

        log.info("Найдено {} вещей для владельца с ID {}", ownedItems.size(), ownerId);
        return ownedItems;
    }

    public List<Item> search(String text) {
        if (text == null || text.isBlank()) {
            log.info("Пустой поисковый запрос — возвращен пустой список");
            return Collections.emptyList();
        }

        String query = text.toLowerCase();

        List<Item> result = items.values().stream()
                .filter(Objects::nonNull)
                .filter(Item::getAvailable)
                .filter(item -> matches(item, query))
                .collect(Collectors.toList());

        log.info("Поиск '{}' вернул {} вещей", text, result.size());
        return result;
    }
}