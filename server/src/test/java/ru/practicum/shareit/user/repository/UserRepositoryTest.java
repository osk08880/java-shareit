package ru.practicum.shareit.user.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = userRepository.save(User.builder()
                .name("Alice")
                .email("alice@example.com")
                .build());

        user2 = userRepository.save(User.builder()
                .name("Bob")
                .email("bob@example.com")
                .build());
    }

    @Test
    void save_savesUserCorrectly() {
        User user = User.builder()
                .name("Charlie")
                .email("charlie@example.com")
                .build();

        User savedUser = userRepository.save(user);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getName()).isEqualTo("Charlie");
        assertThat(savedUser.getEmail()).isEqualTo("charlie@example.com");
    }

    @Test
    void findByEmail_returnsUser() {
        Optional<User> found = userRepository.findByEmail("alice@example.com");
        assertThat(found).isPresent();
        assertThat(found.get()).isEqualTo(user1);
    }

    @Test
    void existsByEmail_returnsTrueIfExists() {
        boolean exists = userRepository.existsByEmail("bob@example.com");
        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmail_returnsFalseIfNotExists() {
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");
        assertThat(exists).isFalse();
    }

    @Test
    void findAll_returnsAllUsers() {
        List<User> users = userRepository.findAll();
        assertThat(users).hasSize(2);
        assertThat(users).containsExactlyInAnyOrder(user1, user2);
    }

    @Test
    void delete_removesUser() {
        userRepository.delete(user1);
        List<User> users = userRepository.findAll();
        assertThat(users).hasSize(1);
        assertThat(users.get(0)).isEqualTo(user2);
    }

    @Test
    void findById_returnsUser() {
        Optional<User> found = userRepository.findById(user1.getId());
        assertThat(found).isPresent();
        assertThat(found.get()).isEqualTo(user1);
    }

    @Test
    void findById_returnsEmptyIfNotFound() {
        Optional<User> found = userRepository.findById(999L);
        assertThat(found).isEmpty();
    }

}
