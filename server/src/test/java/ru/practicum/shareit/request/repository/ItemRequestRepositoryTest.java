package ru.practicum.shareit.request.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ItemRequestRepositoryTest {

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    private User requestor;
    private User otherUser;
    private ItemRequest request1;
    private ItemRequest request2;

    @BeforeEach
    void setUp() {
        requestor = userRepository.save(User.builder()
                .name("Alice")
                .email("alice@example.com")
                .build());

        otherUser = userRepository.save(User.builder()
                .name("Bob")
                .email("bob@example.com")
                .build());

        request1 = itemRequestRepository.save(ItemRequest.builder()
                .description("Need a drill")
                .requestor(requestor)
                .created(LocalDateTime.now().minusDays(1))
                .build());

        request2 = itemRequestRepository.save(ItemRequest.builder()
                .description("Need a hammer")
                .requestor(otherUser)
                .created(LocalDateTime.now())
                .build());

        itemRepository.save(Item.builder()
                .name("Drill")
                .description("Powerful drill")
                .available(true)
                .owner(requestor)
                .request(request1)
                .build());
    }

    @Test
    void findAllByRequestorOrderByCreatedDesc_returnsUserRequests() {
        List<ItemRequest> requests = itemRequestRepository.findAllByRequestorOrderByCreatedDesc(requestor);

        assertThat(requests).hasSize(1);
        assertThat(requests.get(0).getRequestor()).isEqualTo(requestor);
        assertThat(requests.get(0).getDescription()).isEqualTo("Need a drill");
    }

    @Test
    void findAllByRequestorIdNotOrderByCreatedDesc_returnsOtherUsersRequests() {
        List<ItemRequest> requests = itemRequestRepository.findAllByRequestorIdNotOrderByCreatedDesc(requestor.getId());

        assertThat(requests).hasSize(1);
        assertThat(requests.get(0).getRequestor()).isEqualTo(otherUser);
        assertThat(requests.get(0).getDescription()).isEqualTo("Need a hammer");
    }
}
