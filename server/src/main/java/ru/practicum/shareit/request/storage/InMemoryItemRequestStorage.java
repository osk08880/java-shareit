package ru.practicum.shareit.request.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class InMemoryItemRequestStorage {

    private final Map<Long, ItemRequest> requests = new HashMap<>();
    private long idCounter = 1;

    public ItemRequest create(ItemRequest request) {
        request.setId(idCounter++);
        requests.put(request.getId(), request);
        log.info("Запрос создан с ID: {}", request.getId());
        return request;
    }

    public Optional<ItemRequest> findById(Long requestId) {
        return Optional.ofNullable(requests.get(requestId));
    }

    public List<ItemRequest> findAllByRequestorIdNotOrderByCreatedDesc(Long requestorId) {
        return requests.values().stream()
                .filter(r -> !r.getRequestor().getId().equals(requestorId))
                .sorted((r1, r2) -> r2.getCreated().compareTo(r1.getCreated()))
                .collect(Collectors.toList());
    }

    public List<ItemRequest> findAllByRequestorOrderByCreatedDesc(User requestor) {
        return requests.values().stream()
                .filter(r -> r.getRequestor().equals(requestor))
                .sorted((r1, r2) -> r2.getCreated().compareTo(r1.getCreated()))
                .collect(Collectors.toList());
    }

    public Optional<ItemRequest> update(ItemRequest request) {
        if (!requests.containsKey(request.getId())) {
            return Optional.empty();
        }
        requests.put(request.getId(), request);
        return Optional.of(request);
    }
}