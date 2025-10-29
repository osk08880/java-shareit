package ru.practicum.shareit.item.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class InMemoryItemHistoryStorage {
    private final Map<Long, LinkedHashSet<Long>> history = new HashMap<>();

    public void addView(Long userId, Long itemId) {
        history.computeIfAbsent(userId, k -> new LinkedHashSet<>());
        LinkedHashSet<Long> items = history.get(userId);
        if (items.contains(itemId)) {
            items.remove(itemId);
        }
        items.add(itemId);
        log.info("Пользователь {} просмотрел предмет {}", userId, itemId);
    }

    public List<Long> getHistory(Long userId) {
        LinkedHashSet<Long> items = history.getOrDefault(userId, new LinkedHashSet<>());
        log.info("История просмотров пользователя {}: {}", userId, items);
        return new ArrayList<>(items);
    }
}
