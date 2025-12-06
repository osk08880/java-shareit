package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class ItemRequestServiceIntegrationTest {

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRequestRepository requestRepository;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = userRepository.save(User.builder().name("Alice").email("alice@example.com").build());
        user2 = userRepository.save(User.builder().name("Bob").email("bob@example.com").build());
    }

    @Test
    void createRequest_success() {
        ItemRequestDto dto = ItemRequestDto.builder()
                .description("Need a hammer")
                .build();

        ItemRequestDto created = itemRequestService.create(dto, user1.getId()); // исправлено

        assertNotNull(created.getId());
        assertEquals("Need a hammer", created.getDescription());
    }

    @Test
    void getOwnRequests_returnsCorrectList() {
        ItemRequest request1 = requestRepository.save(ItemRequest.builder()
                .description("Request 1")
                .requestor(user1)
                .created(LocalDateTime.now())
                .build());

        ItemRequest request2 = requestRepository.save(ItemRequest.builder()
                .description("Request 2")
                .requestor(user1)
                .created(LocalDateTime.now())
                .build());

        List<ItemRequestDto> requests = itemRequestService.getOwnRequests(user1.getId()); // исправлено

        assertEquals(2, requests.size());
        assertTrue(requests.stream().anyMatch(r -> r.getDescription().equals("Request 1")));
        assertTrue(requests.stream().anyMatch(r -> r.getDescription().equals("Request 2")));
    }

    @Test
    void getAllRequests_excludesUserOwnRequests() {
        requestRepository.save(ItemRequest.builder()
                .description("Request 1")
                .requestor(user1)
                .created(LocalDateTime.now())
                .build());

        requestRepository.save(ItemRequest.builder()
                .description("Request 2")
                .requestor(user2)
                .created(LocalDateTime.now())
                .build());

        List<ItemRequestDto> requests = itemRequestService.getAllRequests(user1.getId()); // исправлено

        assertEquals(1, requests.size());
        assertEquals("Request 2", requests.get(0).getDescription());
    }

    @Test
    void getRequestById_returnsCorrectRequest() {
        ItemRequest request = requestRepository.save(ItemRequest.builder()
                .description("Special request")
                .requestor(user2)
                .created(LocalDateTime.now())
                .build());

        ItemRequestDto dto = itemRequestService.getRequestById(request.getId(), user1.getId()); // исправлено

        assertEquals(request.getId(), dto.getId());
        assertEquals("Special request", dto.getDescription());
        assertEquals(user2.getId(), dto.getRequestor().getId());
    }
}
