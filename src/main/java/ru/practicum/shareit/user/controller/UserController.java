package ru.practicum.shareit.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public UserDto create(@Valid @RequestBody UserDto userDto) {
        log.info("POST /users - создание пользователя: {}", userDto);
        return userService.create(userDto);
    }

    @GetMapping
    public List<UserDto> findAll() {
        log.info("GET /users - получение всех пользователей");
        return userService.findAll();
    }

    @GetMapping("/{userId}")
    public UserDto findById(@PathVariable Long userId) {
        log.info("GET /users/{} - получение пользователя", userId);
        return userService.findById(userId);
    }

    @PatchMapping("/{userId}")
    public UserDto update(@PathVariable Long userId, @Valid  @RequestBody UserDto userDto) {
        log.info("PATCH /users/{} - обновление пользователя: {}", userId, userDto);
        return userService.update(userId, userDto);
    }

    @DeleteMapping("/{userId}")
    public void delete(@PathVariable Long userId) {
        log.info("DELETE /users/{} - удаление пользователя", userId);
        userService.delete(userId);
    }
}