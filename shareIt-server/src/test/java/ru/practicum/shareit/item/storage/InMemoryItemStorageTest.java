package ru.practicum.shareit.item.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryItemStorageTest {

    private InMemoryItemStorage storage;
    private User user1;
    private Item item1;
    private Item item2;

    @BeforeEach
    void setUp() {
        storage = new InMemoryItemStorage();
        user1 = User.builder().id(1L).name("Alice").email("alice@example.com").build();

        item1 = Item.builder().name("Hammer").description("Steel hammer").available(true).owner(user1).build();
        item2 = Item.builder().name("Drill").description("Electric drill").available(true).owner(user1).build();

        storage.create(item1);
        storage.create(item2);
    }

    @Test
    void create_assignsIdAndStoresItem() {
        Item item = Item.builder().name("Screwdriver").description("Flat screwdriver").available(true).owner(user1).build();
        Item created = storage.create(item);

        assertThat(created.getId()).isNotNull();
        assertThat(storage.findById(created.getId())).isPresent();
    }

    @Test
    void findById_existingItem_returnsItem() {
        Optional<Item> found = storage.findById(item1.getId());
        assertThat(found).isPresent();
        assertThat(found.get()).isEqualTo(item1);
    }

    @Test
    void findById_nonExistingItem_returnsEmpty() {
        Optional<Item> found = storage.findById(999L);
        assertThat(found).isEmpty();
    }

    @Test
    void update_existingItem_returnsUpdated() {
        item1.setDescription("Updated hammer");
        Optional<Item> updated = storage.update(item1);

        assertThat(updated).isPresent();
        assertThat(updated.get().getDescription()).isEqualTo("Updated hammer");
    }

    @Test
    void update_nonExistingItem_returnsEmpty() {
        Item fake = Item.builder().id(999L).name("Fake").description("Fake").available(true).owner(user1).build();
        Optional<Item> updated = storage.update(fake);

        assertThat(updated).isEmpty();
    }

    @Test
    void findAllByOwner_returnsItemsForOwner() {
        List<Item> items = storage.findAllByOwner(user1.getId());
        assertThat(items).hasSize(2);
        assertThat(items).containsExactlyInAnyOrder(item1, item2);
    }

    @Test
    void search_findsByNameOrDescription() {
        List<Item> results = storage.search("hammer");
        assertThat(results).hasSize(1);
        assertThat(results.get(0)).isEqualTo(item1);
    }

    @Test
    void search_emptyQuery_returnsEmptyList() {
        List<Item> results = storage.search("");
        assertThat(results).isEmpty();
    }
}
