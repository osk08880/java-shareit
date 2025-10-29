package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.DuplicateEmailException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.InMemoryUserStorage;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final InMemoryUserStorage userStorage;

    @Override
    public UserDto create(UserDto userDto) {
        validateUserDto(userDto);

        if (userStorage.findAll().stream().anyMatch(u -> Objects.equals(u.getEmail(), userDto.getEmail()))) {
            log.warn("Попытка создать пользователя с уже существующим email: {}", userDto.getEmail());
            throw new DuplicateEmailException("Email должен быть уникальным");
        }

        User user = UserMapper.toUser(userDto);
        User createdUser = userStorage.create(user);
        log.info("Создан пользователь с ID: {}", createdUser.getId());
        return UserMapper.toUserDto(createdUser);
    }

    @Override
    public UserDto update(Long userId, UserDto userDto) {
        User existingUser = userStorage.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь с ID {} не найден", userId);
                    return new NotFoundException("Пользователь не найден");
                });

        String newName = (userDto.getName() != null && !userDto.getName().trim().isEmpty())
                ? userDto.getName().trim()
                : existingUser.getName();

        String newEmail = (userDto.getEmail() != null && !userDto.getEmail().trim().isEmpty())
                ? userDto.getEmail().trim()
                : existingUser.getEmail();

        if (!newEmail.equals(existingUser.getEmail()) &&
                userStorage.findAll().stream()
                        .anyMatch(u -> !Objects.equals(u.getId(), userId) &&
                                Objects.equals(u.getEmail(), newEmail))) {
            log.warn("Попытка обновления пользователя с дублирующим email: {}", newEmail);
            throw new DuplicateEmailException("Email должен быть уникальным");
        }

        if (!newEmail.equals(existingUser.getEmail()) && !newEmail.contains("@")) {
            log.warn("Неверный формат email: {}", newEmail);
            throw new IllegalArgumentException("Неверный формат email");
        }

        User updatedUser = User.builder()
                .id(userId)
                .name(newName)
                .email(newEmail)
                .build();

        userStorage.update(updatedUser)
                .orElseThrow(() -> {
                    log.error("Не удалось обновить пользователя с ID {}", userId);
                    return new NotFoundException("Обновление не удалось");
                });

        log.info("Пользователь обновлён: {}", updatedUser);
        return UserMapper.toUserDto(updatedUser);
    }

    @Override
    public UserDto findById(Long userId) {
        User user = userStorage.findById(userId).orElseThrow(() -> {
            log.warn("Пользователь с ID {} не найден", userId);
            return new NotFoundException("Пользователь не найден");
        });
        log.info("Найден пользователь: {}", user);
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> findAll() {
        List<UserDto> users = userStorage.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
        log.info("Найдено {} пользователей", users.size());
        return users;
    }

    @Override
    public void delete(Long userId) {
        if (!userStorage.findById(userId).isPresent()) {
            log.warn("Попытка удалить несуществующего пользователя ID {}", userId);
            throw new NotFoundException("Пользователь не найден");
        }
        userStorage.delete(userId);
        log.info("Удален пользователь с ID {}", userId);
    }

    private void validateUserDto(UserDto userDto) {
        if (userDto.getName() == null || userDto.getName().trim().isEmpty()) {
            log.warn("Имя пользователя пустое");
            throw new IllegalArgumentException("Имя пользователя обязательно и не может быть пустым");
        }
        if (userDto.getEmail() == null || userDto.getEmail().trim().isEmpty()) {
            log.warn("Email пользователя пустой");
            throw new IllegalArgumentException("Email пользователя обязателен и не может быть пустым");
        }
        if (!userDto.getEmail().contains("@")) {
            log.warn("Некорректный формат email: {}", userDto.getEmail());
            throw new IllegalArgumentException("Неверный формат email");
        }
    }
}
