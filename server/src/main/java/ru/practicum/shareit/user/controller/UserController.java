package ru.practicum.shareit.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public UserDto create(@RequestBody UserDto userDto) {
        log.info("SERVER: Создание пользователя {}", userDto);
        return userService.create(userDto);
    }

    @GetMapping
    public List<UserDto> findAll() {
        log.info("SERVER: Получение всех пользователей");
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public UserDto findById(@PathVariable Long id) {
        log.info("SERVER: Получение пользователя id={}", id);
        return userService.findById(id);
    }

    @PatchMapping("/{id}")
    public UserDto update(@PathVariable Long id,
                          @RequestBody UserDto userDto) {
        log.info("SERVER: Обновление пользователя id={} данные={}", id, userDto);
        return userService.update(id, userDto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        log.info("SERVER: Удаление пользователя id={}", id);
        userService.delete(id);
    }
}
