package ru.practicum.shareit.request.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.*;

@Slf4j
@Component
public class InMemoryItemRequestStorage {
    private final Map<Long, ItemRequest> requests = new HashMap<>();
    private long idCounter = 1;

    public ItemRequest create(ItemRequest request) {
        request.setId(idCounter++);
        requests.put(request.getId(), request);
        log.info("Создан запрос вещи: {}", request);
        return request;
    }

    public Optional<ItemRequest> findById(Long requestId) {
        if (requestId == null) {
            log.warn("Попытка поиска запроса вещи с null ID");
            return Optional.empty();
        }

        ItemRequest request = requests.get(requestId);
        if (request == null) {
            log.warn("Запрос вещи с ID {} не найден", requestId);
            return Optional.empty();
        }
        log.info("Найден запрос вещи с ID {}: {}", requestId, request);
        return Optional.of(request);
    }

    public List<ItemRequest> findAll() {
        List<ItemRequest> allRequests = new ArrayList<>(requests.values());
        log.info("Возвращено {} запросов вещей", allRequests.size());
        return allRequests;
    }

    public void delete(Long requestId) {
        if (requestId == null) {
            log.warn("Попытка удалить запрос вещи с null ID");
            return;
        }

        if (requests.remove(requestId) != null) {
            log.info("Удалён запрос вещи с ID {}", requestId);
        } else {
            log.warn("Попытка удалить несуществующий запрос вещи с ID {}", requestId);
        }
    }
}
