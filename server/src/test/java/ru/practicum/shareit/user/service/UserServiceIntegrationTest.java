package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class UserServiceIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void createUser_success() {
        User user = User.builder()
                .name("Alice")
                .email("alice@example.com")
                .build();

        User saved = userRepository.save(user);

        assertNotNull(saved.getId(), "ID пользователя должен быть сгенерирован");
        assertEquals("Alice", saved.getName());
        assertEquals("alice@example.com", saved.getEmail());
    }

    @Test
    void updateUser_success() {
        User user = userRepository.save(User.builder()
                .name("Bob")
                .email("bob@example.com")
                .build());

        user.setName("Bobby");
        User updated = userRepository.save(user);

        assertEquals("Bobby", updated.getName());
    }

    @Test
    void findUserById_success() {
        User user = userRepository.save(User.builder()
                .name("Charlie")
                .email("charlie@example.com")
                .build());

        User found = userRepository.findById(user.getId()).orElse(null);

        assertNotNull(found);
        assertEquals("Charlie", found.getName());
    }

    @Test
    void deleteUser_success() {
        User user = userRepository.save(User.builder().name("Charlie").email("charlie@example.com").build());
        Long userId = user.getId();

        userRepository.deleteById(userId);

        assertFalse(userRepository.findById(userId).isPresent(), "Пользователь должен быть удален");
    }

    @Autowired
    private UserService userService;

    @Test
    void findAllUsers_returnsList() {
        userService.create(UserDto.builder().name("Alice").email("alice@example.com").build());
        userService.create(UserDto.builder().name("Bob").email("bob@example.com").build());

        List<UserDto> users = userService.findAll();

        assertEquals(2, users.size());
        assertTrue(users.stream().anyMatch(u -> u.getName().equals("Alice")));
        assertTrue(users.stream().anyMatch(u -> u.getName().equals("Bob")));
    }
}
