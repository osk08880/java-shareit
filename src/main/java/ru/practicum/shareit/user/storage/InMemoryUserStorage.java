package ru.practicum.shareit.user.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Slf4j
@Component
public class InMemoryUserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private long idCounter = 1;

    public User create(User user) {
        user.setId(idCounter++);
        users.put(user.getId(), user);
        log.info("Пользователь добавлен в хранилище с ID {}", user.getId());
        return user;
    }

    public Optional<User> update(User user) {
        if (!users.containsKey(user.getId())) {
            log.warn("Попытка обновления несуществующего пользователя ID {}", user.getId());
            return Optional.empty();
        }
        users.put(user.getId(), user);
        log.info("Пользователь обновлен в хранилище: {}", user);
        return Optional.of(user);
    }

    public Optional<User> findById(Long userId) {
        User user = users.get(userId);
        if (user != null) {
            log.info("Пользователь найден в хранилище: {}", user);
        } else {
            log.warn("Пользователь с ID {} не найден в хранилище", userId);
        }
        return Optional.ofNullable(user);
    }

    public List<User> findAll() {
        log.info("Возвращено {} пользователей из хранилища", users.size());
        return new ArrayList<>(users.values());
    }

    public void delete(Long userId) {
        users.remove(userId);
        log.info("Пользователь с ID {} удален из хранилища", userId);
    }
}
