package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.DuplicateEmailException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    private static final String EMAIL_SYMBOL = "@";
    private static final String MSG_USER_NOT_FOUND = "Пользователь не найден";
    private static final String MSG_EMAIL_DUPLICATE = "Email должен быть уникальным";
    private static final String MSG_INVALID_EMAIL = "Неверный формат email";
    private static final String MSG_EMPTY_NAME = "Имя пользователя обязательно и не может быть пустым";
    private static final String MSG_EMPTY_EMAIL = "Email пользователя обязателен и не может быть пустым";

    @Override
    public UserDto create(UserDto userDto) {
        validateUserDto(userDto);

        if (userRepository.existsByEmail(userDto.getEmail())) {
            log.warn("Попытка создать пользователя с уже существующим email: {}", userDto.getEmail());
            throw new DuplicateEmailException(MSG_EMAIL_DUPLICATE);
        }

        log.debug("Создание пользователя с данными: {}", userDto);
        User user = UserMapper.toUser(userDto);
        User createdUser = userRepository.save(user);
        log.info("Создан пользователь с ID: {}", createdUser.getId());
        return UserMapper.toUserDto(createdUser);
    }

    @Override
    public UserDto update(Long userId, UserDto userDto) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь с ID {} не найден", userId);
                    return new NotFoundException(MSG_USER_NOT_FOUND);
                });

        String newName = (userDto.getName() != null && !userDto.getName().trim().isEmpty())
                ? userDto.getName().trim()
                : existingUser.getName();

        String newEmail = (userDto.getEmail() != null && !userDto.getEmail().trim().isEmpty())
                ? userDto.getEmail().trim()
                : existingUser.getEmail();

        log.debug("Обновление пользователя ID {}: новое имя={}, новый email={}", userId, newName, newEmail);

        if (!newEmail.equals(existingUser.getEmail()) && userRepository.existsByEmail(newEmail)) {
            log.warn("Попытка обновления пользователя с дублирующим email: {}", newEmail);
            throw new DuplicateEmailException(MSG_EMAIL_DUPLICATE);
        }

        if (!newEmail.equals(existingUser.getEmail()) && !newEmail.contains(EMAIL_SYMBOL)) {
            log.warn("Неверный формат email: {}", newEmail);
            throw new IllegalArgumentException(MSG_INVALID_EMAIL);
        }

        User updatedUser = User.builder()
                .id(userId)
                .name(newName)
                .email(newEmail)
                .build();

        User savedUser = userRepository.save(updatedUser);
        log.info("Пользователь обновлён: {}", savedUser);
        return UserMapper.toUserDto(savedUser);
    }

    @Override
    public UserDto findById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь с ID {} не найден", userId);
                    return new NotFoundException(MSG_USER_NOT_FOUND);
                });
        log.info("Найден пользователь: {}", user);
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> findAll() {
        List<UserDto> users = userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
        log.info("Найдено {} пользователей", users.size());
        return users;
    }

    @Override
    public void delete(Long userId) {
        if (!userRepository.existsById(userId)) {
            log.warn("Попытка удалить несуществующего пользователя ID {}", userId);
            throw new NotFoundException(MSG_USER_NOT_FOUND);
        }
        userRepository.deleteById(userId);
        log.info("Удален пользователь с ID {}", userId);
    }

    private void validateUserDto(UserDto userDto) {
        if (userDto.getName() == null || userDto.getName().trim().isEmpty()) {
            log.warn("Имя пользователя пустое");
            throw new IllegalArgumentException(MSG_EMPTY_NAME);
        }
        if (userDto.getEmail() == null || userDto.getEmail().trim().isEmpty()) {
            log.warn("Email пользователя пустой");
            throw new IllegalArgumentException(MSG_EMPTY_EMAIL);
        }
        if (!userDto.getEmail().contains(EMAIL_SYMBOL)) {
            log.warn("Некорректный формат email: {}", userDto.getEmail());
            throw new IllegalArgumentException(MSG_INVALID_EMAIL);
        }
    }
}
