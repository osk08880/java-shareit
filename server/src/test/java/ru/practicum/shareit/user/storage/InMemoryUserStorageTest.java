package ru.practicum.shareit.user.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InMemoryUserStorageTest {

    private InMemoryUserStorage storage;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        storage = new InMemoryUserStorage();

        user1 = User.builder().name("Alice").email("alice@example.com").build();
        user2 = User.builder().name("Bob").email("bob@example.com").build();

        storage.create(user1);
        storage.create(user2);
    }

    @Test
    void create_addsUserAndAssignsId() {
        User user = User.builder().name("Charlie").email("charlie@example.com").build();
        User created = storage.create(user);

        assertThat(created.getId()).isNotNull();
        assertThat(storage.findAll()).contains(created);
    }

    @Test
    void create_nullUser_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> storage.create(null));
    }

    @Test
    void update_existingUser_returnsUpdated() {
        user1.setName("Alice Updated");
        Optional<User> updated = storage.update(user1);

        assertThat(updated).isPresent();
        assertThat(updated.get().getName()).isEqualTo("Alice Updated");
    }

    @Test
    void update_nonExistingUser_returnsEmpty() {
        User fakeUser = User.builder().id(999L).name("Fake").email("fake@example.com").build();
        Optional<User> result = storage.update(fakeUser);

        assertThat(result).isEmpty();
    }

    @Test
    void findById_existing_returnsUser() {
        Optional<User> found = storage.findById(user1.getId());
        assertThat(found).isPresent();
        assertThat(found.get()).isEqualTo(user1);
    }

    @Test
    void findById_nonExisting_returnsEmpty() {
        Optional<User> found = storage.findById(999L);
        assertThat(found).isEmpty();
    }

    @Test
    void findAll_returnsAllUsers() {
        List<User> users = storage.findAll();
        assertThat(users).hasSize(2).containsExactlyInAnyOrder(user1, user2);
    }

    @Test
    void delete_existingUser_removesUser() {
        storage.delete(user1.getId());
        List<User> users = storage.findAll();
        assertThat(users).hasSize(1).doesNotContain(user1);
    }

    @Test
    void delete_nonExistingUser_doesNothing() {
        storage.delete(999L);
        List<User> users = storage.findAll();
        assertThat(users).hasSize(2);
    }

    @Test
    void delete_nullId_doesNothing() {
        storage.delete(null);
        List<User> users = storage.findAll();
        assertThat(users).hasSize(2);
    }
}
