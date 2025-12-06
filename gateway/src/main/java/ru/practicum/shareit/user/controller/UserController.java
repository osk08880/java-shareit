package ru.practicum.shareit.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.client.UserClient;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(UserController.BASE_PATH)
@RequiredArgsConstructor
public class UserController {

    private final UserClient userClient;

    public static final String BASE_PATH = "/users";

    @PostMapping
    public ResponseEntity<Object> create(@RequestBody UserDto userDto) {
        log.info("GATEWAY: POST {} {}", BASE_PATH, userDto);
        return userClient.create(userDto);
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> findAll() {
        log.info("GATEWAY: GET {}", BASE_PATH);
        return userClient.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> findById(@PathVariable Long id) {
        log.info("GATEWAY: GET {}/{}", BASE_PATH, id);
        return userClient.findById(id);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> update(@PathVariable Long id, @RequestBody UserDto userDto) {
        log.info("GATEWAY: PATCH {}/{} {}", BASE_PATH, id, userDto);
        return userClient.update(id, userDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(@PathVariable Long id) {
        log.info("GATEWAY: DELETE {}/{}", BASE_PATH, id);
        return userClient.delete(id);
    }
}
