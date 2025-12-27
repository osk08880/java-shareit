package ru.practicum.shareit.item.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    private User owner;
    private User otherUser;
    private ItemRequest request;
    private Item item1;
    private Item item2;

    @BeforeEach
    void setUp() {
        owner = userRepository.save(User.builder()
                .name("Owner")
                .email("owner@example.com")
                .build());

        otherUser = userRepository.save(User.builder()
                .name("Other")
                .email("other@example.com")
                .build());

        request = itemRequestRepository.save(ItemRequest.builder()
                .description("Need a drill")
                .requestor(otherUser)
                .created(java.time.LocalDateTime.now())
                .build());

        item1 = itemRepository.save(Item.builder()
                .name("Drill")
                .description("Powerful drill")
                .available(true)
                .owner(owner)
                .request(request)
                .build());

        item2 = itemRepository.save(Item.builder()
                .name("Hammer")
                .description("Strong hammer")
                .available(true)
                .owner(owner)
                .build());
    }

    @Test
    void findByOwnerId_returnsItems() {
        List<Item> items = itemRepository.findByOwnerId(owner.getId());
        assertThat(items).hasSize(2);
        assertThat(items).contains(item1, item2);
    }

    @Test
    void searchAvailableItems_returnsMatchingItems() {
        List<Item> items = itemRepository.searchAvailableItems("drill");
        assertThat(items).hasSize(1);
        assertThat(items.get(0)).isEqualTo(item1);

        List<Item> items2 = itemRepository.searchAvailableItems("strong");
        assertThat(items2).hasSize(1);
        assertThat(items2.get(0)).isEqualTo(item2);
    }

    @Test
    void findByRequestId_returnsItemsLinkedToRequest() {
        List<Item> items = itemRepository.findByRequestId(request.getId());
        assertThat(items).hasSize(1);
        assertThat(items.get(0)).isEqualTo(item1);
    }
}
