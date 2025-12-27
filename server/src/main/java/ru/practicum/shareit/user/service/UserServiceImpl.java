package ru.practicum.shareit.user.service;

import jakarta.transaction.Transactional;
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

    private static final String MSG_USER_NOT_FOUND = "Пользователь не найден";
    private static final String MSG_EMAIL_DUPLICATE = "Email должен быть уникальным";
    private static final String MSG_EMAIL_INVALID = "Email не может быть пустым или некорректным";
    private static final String EMAIL_PATTERN = "@";
    private static final String LOG_PREFIX = "SERVER: ";

    @Override
    @Transactional
    public UserDto create(UserDto dto) {
        log.info("{}Создание пользователя {}", LOG_PREFIX, dto);

        validateEmail(dto.getEmail());

        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            log.warn("{}Попытка создать пользователя с существующим email: {}", LOG_PREFIX, dto.getEmail());
            throw new DuplicateEmailException(MSG_EMAIL_DUPLICATE);
        }

        User user = UserMapper.toUser(dto);
        user = userRepository.save(user);

        log.info("{}Пользователь создан: {}", LOG_PREFIX, user);
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto update(Long userId, UserDto userDto) {
        log.info("{}Обновление пользователя с ID {}: {}", LOG_PREFIX, userId, userDto);

        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("{}Пользователь не найден для обновления: {}", LOG_PREFIX, userId);
                    return new NotFoundException(MSG_USER_NOT_FOUND);
                });

        String newName = (userDto.getName() != null) ? userDto.getName() : existingUser.getName();
        String newEmail = (userDto.getEmail() != null) ? userDto.getEmail() : existingUser.getEmail();

        if (!newEmail.equals(existingUser.getEmail()) &&
                userRepository.findByEmail(newEmail).isPresent()) {
            log.warn("{}Попытка обновить пользователя с дублирующим email: {}", LOG_PREFIX, newEmail);
            throw new DuplicateEmailException(MSG_EMAIL_DUPLICATE);
        }

        validateEmail(newEmail);

        User updatedUser = User.builder()
                .id(userId)
                .name(newName)
                .email(newEmail)
                .build();

        User savedUser = userRepository.save(updatedUser);
        log.info("{}Пользователь обновлён: {}", LOG_PREFIX, savedUser);

        return UserMapper.toUserDto(savedUser);
    }

    @Override
    public UserDto findById(Long userId) {
        log.info("{}Поиск пользователя по ID {}", LOG_PREFIX, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("{}Пользователь не найден: {}", LOG_PREFIX, userId);
                    return new NotFoundException(MSG_USER_NOT_FOUND);
                });

        log.info("{}Пользователь найден: {}", LOG_PREFIX, user);
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> findAll() {
        log.info("{}Получение списка всех пользователей", LOG_PREFIX);
        List<UserDto> users = userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
        log.info("{}Найдено пользователей: {}", LOG_PREFIX, users.size());
        return users;
    }

    @Override
    public void delete(Long userId) {
        log.info("{}Удаление пользователя с ID {}", LOG_PREFIX, userId);

        if (!userRepository.existsById(userId)) {
            log.warn("{}Пользователь не найден для удаления: {}", LOG_PREFIX, userId);
            throw new NotFoundException(MSG_USER_NOT_FOUND);
        }

        userRepository.deleteById(userId);
        log.info("{}Пользователь с ID {} успешно удалён", LOG_PREFIX, userId);
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank() || !email.contains(EMAIL_PATTERN)) {
            log.warn("{}Некорректный email: {}", LOG_PREFIX, email);
            throw new IllegalArgumentException(MSG_EMAIL_INVALID);
        }
    }
}
