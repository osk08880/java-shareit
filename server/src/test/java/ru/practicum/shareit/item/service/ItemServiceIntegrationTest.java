package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
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
class ItemServiceIntegrationTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemRequestRepository requestRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CommentRepository commentRepository;

    private User user1;
    private User user2;
    private ItemRequest request;

    @BeforeEach
    void setUp() {
        user1 = userRepository.save(User.builder().name("Alice").email("alice@example.com").build());
        user2 = userRepository.save(User.builder().name("Bob").email("bob@example.com").build());

        request = requestRepository.save(
                ItemRequest.builder()
                        .description("Need a drill")
                        .requestor(user2)
                        .created(LocalDateTime.now())
                        .build()
        );
    }

    @Test
    void createItem_withRequest_success() {
        ItemDto itemDto = ItemDto.builder()
                .name("Drill")
                .description("Powerful drill")
                .available(true)
                .requestId(request.getId())
                .build();

        ItemDto created = itemService.create(user1.getId(), itemDto, request.getId());

        assertNotNull(created.getId());
        assertEquals("Drill", created.getName());
        assertEquals(request.getId(), created.getRequest().getId());
    }

    @Test
    void updateItem_success() {
        Item item = itemRepository.save(Item.builder()
                .name("Old Drill")
                .description("Old description")
                .available(true)
                .owner(user1)
                .build());

        ItemDto updatedDto = ItemDto.builder()
                .name("New Drill")
                .description("New description")
                .available(false)
                .build();

        ItemDto result = itemService.update(user1.getId(), item.getId(), updatedDto);

        assertEquals("New Drill", result.getName());
        assertEquals("New description", result.getDescription());
        assertFalse(result.getAvailable());
    }

    @Test
    void findById_withCommentsAndBookings() {
        Item item = itemRepository.save(Item.builder()
                .name("Saw")
                .description("Electric saw")
                .available(true)
                .owner(user1)
                .build());

        Booking booking = bookingRepository.save(Booking.builder()
                .item(item)
                .booker(user2)
                .status(BookingStatus.APPROVED)
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().minusDays(1))
                .build());

        CommentDto commentDto = CommentDto.builder()
                .text("Good item")
                .build();

        itemService.addComment(commentDto, user2.getId(), item.getId());

        ItemDto dto = itemService.findById(user1.getId(), item.getId());

        assertNotNull(dto.getLastBooking());
        assertEquals(booking.getId(), dto.getLastBooking().getId());
        assertEquals(1, dto.getComments().size());
        assertEquals("Good item", dto.getComments().get(0).getText());
    }

    @Test
    void findAllByOwner_returnsAllItems() {
        Item item1 = itemRepository.save(Item.builder().name("Item1").description("Desc1").available(true).owner(user1).build());
        Item item2 = itemRepository.save(Item.builder().name("Item2").description("Desc2").available(true).owner(user1).build());

        List<ItemDto> items = itemService.findAllByOwner(user1.getId());

        assertEquals(2, items.size());
    }

    @Test
    void searchItems_returnsMatching() {
        itemRepository.save(Item.builder().name("Hammer").description("Strong hammer").available(true).owner(user1).build());
        itemRepository.save(Item.builder().name("Screwdriver").description("Flathead").available(true).owner(user1).build());

        List<ItemDto> result = itemService.search("hammer");

        assertEquals(1, result.size());
        assertEquals("Hammer", result.get(0).getName());
    }

    @Test
    void addComment_withBooking_success() {
        Item item = itemRepository.save(Item.builder()
                .name("Drill")
                .description("Drill description")
                .available(true)
                .owner(user1)
                .build());

        Booking booking = bookingRepository.save(Booking.builder()
                .item(item)
                .booker(user2)
                .status(BookingStatus.APPROVED)
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().minusDays(1))
                .build());

        CommentDto commentDto = CommentDto.builder()
                .text("Nice drill")
                .build();

        CommentDto result = itemService.addComment(commentDto, user2.getId(), item.getId());

        assertNotNull(result.getId());
        assertEquals("Nice drill", result.getText());
    }
}
