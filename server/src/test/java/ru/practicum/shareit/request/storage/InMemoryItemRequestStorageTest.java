package ru.practicum.shareit.request.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryItemRequestStorageTest {

    private InMemoryItemRequestStorage storage;
    private User user1;
    private User user2;
    private ItemRequest request1;
    private ItemRequest request2;
    private ItemRequest request3;

    @BeforeEach
    void setUp() {
        storage = new InMemoryItemRequestStorage();

        user1 = User.builder().id(1L).name("Alice").email("alice@example.com").build();
        user2 = User.builder().id(2L).name("Bob").email("bob@example.com").build();

        request1 = ItemRequest.builder().description("Request 1").requestor(user1).created(LocalDateTime.now().minusDays(1)).build();
        request2 = ItemRequest.builder().description("Request 2").requestor(user1).created(LocalDateTime.now()).build();
        request3 = ItemRequest.builder().description("Request 3").requestor(user2).created(LocalDateTime.now()).build();

        storage.create(request1);
        storage.create(request2);
        storage.create(request3);
    }

    @Test
    void create_assignsIdAndStoresRequest() {
        ItemRequest newRequest = ItemRequest.builder().description("New Request").requestor(user1).created(LocalDateTime.now()).build();
        ItemRequest created = storage.create(newRequest);

        assertThat(created.getId()).isNotNull();
        assertThat(storage.findById(created.getId())).isPresent();
    }

    @Test
    void findById_existingRequest_returnsRequest() {
        Optional<ItemRequest> found = storage.findById(request1.getId());
        assertThat(found).isPresent();
        assertThat(found.get()).isEqualTo(request1);
    }

    @Test
    void findById_nonExistingRequest_returnsEmpty() {
        Optional<ItemRequest> found = storage.findById(999L);
        assertThat(found).isEmpty();
    }

    @Test
    void update_existingRequest_returnsUpdated() {
        request1.setDescription("Updated Description");
        Optional<ItemRequest> updated = storage.update(request1);

        assertThat(updated).isPresent();
        assertThat(updated.get().getDescription()).isEqualTo("Updated Description");
    }

    @Test
    void update_nonExistingRequest_returnsEmpty() {
        ItemRequest fake = ItemRequest.builder().id(999L).description("Fake").requestor(user1).created(LocalDateTime.now()).build();
        Optional<ItemRequest> updated = storage.update(fake);

        assertThat(updated).isEmpty();
    }

    @Test
    void findAllByRequestorOrderByCreatedDesc_returnsOnlyRequestorRequestsSorted() {
        List<ItemRequest> results = storage.findAllByRequestorOrderByCreatedDesc(user1);

        assertThat(results).hasSize(2);
        assertThat(results.get(0)).isEqualTo(request2);
        assertThat(results.get(1)).isEqualTo(request1);
    }

    @Test
    void findAllByRequestorIdNotOrderByCreatedDesc_returnsOtherUsersRequestsSorted() {
        List<ItemRequest> results = storage.findAllByRequestorIdNotOrderByCreatedDesc(user1.getId());

        assertThat(results).hasSize(1);
        assertThat(results.get(0)).isEqualTo(request3);
    }
}
