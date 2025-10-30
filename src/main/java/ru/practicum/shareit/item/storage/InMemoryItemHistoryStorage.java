package ru.practicum.shareit.item.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class InMemoryItemHistoryStorage {

    private final Map<Long, LinkedHashSet<Long>> history = new HashMap<>();

    public void addView(Long userId, Long itemId) {
        if (userId == null || itemId == null) {
            log.warn("Попытка добавить просмотр с null значением (userId={}, itemId={})", userId, itemId);
            return;
        }

        history.computeIfAbsent(userId, k -> new LinkedHashSet<>());
        LinkedHashSet<Long> items = history.get(userId);

        if (items.remove(itemId)) {
            log.debug("Предмет {} уже был в истории пользователя {}, перемещён в конец", itemId, userId);
        }

        items.add(itemId);
        log.info("Пользователь {} просмотрел предмет {}", userId, itemId);
    }

    public List<Long> getHistory(Long userId) {
        if (userId == null) {
            log.warn("Запрос истории для userId = null");
            return Collections.emptyList();
        }

        LinkedHashSet<Long> items = history.get(userId);
        if (items == null || items.isEmpty()) {
            log.info("У пользователя {} нет истории просмотров", userId);
            return Collections.emptyList();
        }

        log.info("История просмотров пользователя {}: {}", userId, items);
        return new ArrayList<>(items);
    }
}
