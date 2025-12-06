package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.storage.InMemoryItemHistoryStorage;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock private ItemRepository itemRepository;
    @Mock private UserRepository userRepository;
    @Mock private ItemRequestRepository requestRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private CommentRepository commentRepository;
    @Mock private CommentMapper commentMapper;
    @Mock private InMemoryItemHistoryStorage historyStorage;

    @InjectMocks private ItemServiceImpl itemService;

    private User owner;
    private User booker;
    private ItemRequest request;
    private Item item;
    private ItemDto itemDto;
    private Comment comment;
    private CommentDto commentDto;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setName("Alice");

        booker = new User();
        booker.setId(2L);
        booker.setName("Bob");

        request = new ItemRequest();
        request.setId(10L);
        request.setRequestor(booker);

        item = Item.builder()
                .id(100L)
                .name("Drill")
                .description("Cordless drill")
                .available(true)
                .owner(owner)
                .request(request)
                .build();

        itemDto = ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .requestId(request.getId())
                .build();

        comment = Comment.builder()
                .id(1L)
                .text("Nice drill")
                .authorId(booker.getId())
                .itemId(item.getId())
                .created(LocalDateTime.now())
                .build();

        commentDto = CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(booker.getName())
                .created(comment.getCreated())
                .build();
    }

    @Test
    void createItemSuccess() {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemDto result = itemService.create(owner.getId(), itemDto, request.getId());

        assertNotNull(result);
        assertEquals(item.getId(), result.getId());
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    void createItem_UserNotFound() {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class,
                () -> itemService.create(owner.getId(), itemDto, null));
    }

    @Test
    void createItem_RequestNotFound() {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(requestRepository.findById(request.getId())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> itemService.create(owner.getId(), itemDto, request.getId()));
    }

    @Test
    void createItem_CannotAnswerOwnRequest() {
        request.setRequestor(owner);
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));

        assertThrows(IllegalArgumentException.class,
                () -> itemService.create(owner.getId(), itemDto, request.getId()));
    }

    @Test
    void updateItemSuccess() {
        ItemDto updateDto = ItemDto.builder()
                .name("New Drill")
                .description("Updated")
                .available(false)
                .build();

        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemDto result = itemService.update(owner.getId(), item.getId(), updateDto);

        assertEquals("New Drill", result.getName());
        assertEquals("Updated", result.getDescription());
        assertFalse(result.getAvailable());
    }

    @Test
    void updateItem_NotOwner() {
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        assertThrows(RuntimeException.class,
                () -> itemService.update(booker.getId(), item.getId(), itemDto));
    }

    @Test
    void findById_OwnerGetsBookings() {
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(commentRepository.findByItemIdOrderByCreatedDesc(item.getId()))
                .thenReturn(Collections.singletonList(comment));
        when(commentMapper.toCommentDto(comment)).thenReturn(commentDto);

        when(bookingRepository.findFirstByItemIdAndStatusAndEndBeforeOrderByEndDesc(
                anyLong(), eq(BookingStatus.APPROVED), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        when(bookingRepository.findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(
                anyLong(), eq(BookingStatus.APPROVED), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        ItemDto result = itemService.findById(owner.getId(), item.getId());

        assertEquals(1, result.getComments().size());
        assertNull(result.getLastBooking());
        assertNull(result.getNextBooking());

        verify(historyStorage).addView(owner.getId(), item.getId());
    }

    @Test
    void findById_NonOwnerNoBookings() {
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(commentRepository.findByItemIdOrderByCreatedDesc(item.getId()))
                .thenReturn(Collections.singletonList(comment));
        when(commentMapper.toCommentDto(comment)).thenReturn(commentDto);

        ItemDto result = itemService.findById(booker.getId(), item.getId());

        assertNull(result.getLastBooking());
        assertNull(result.getNextBooking());
    }

    @Test
    void findById_NotFound() {
        when(itemRepository.findById(item.getId())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class,
                () -> itemService.findById(owner.getId(), item.getId()));
    }

    @Test
    void findAllByOwner_Success() {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(itemRepository.findByOwnerId(owner.getId())).thenReturn(Collections.singletonList(item));
        when(bookingRepository.findFirstByItemIdAndStatusAndEndBeforeOrderByEndDesc(anyLong(),
                eq(BookingStatus.APPROVED), any())).thenReturn(Optional.empty());
        when(bookingRepository.findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(anyLong(),
                eq(BookingStatus.APPROVED), any())).thenReturn(Optional.empty());

        List<ItemDto> items = itemService.findAllByOwner(owner.getId());
        assertEquals(1, items.size());
    }

    @Test
    void findAllByOwner_UserNotFound() {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class,
                () -> itemService.findAllByOwner(owner.getId()));
    }

    @Test
    void search_ReturnsEmptyForBlank() {
        List<ItemDto> result = itemService.search(" ");
        assertTrue(result.isEmpty());
    }

    @Test
    void search_Success() {
        when(itemRepository.searchAvailableItems("drill")).thenReturn(Collections.singletonList(item));
        List<ItemDto> result = itemService.search("drill");
        assertEquals(1, result.size());
    }

    @Test
    void addComment_Success() {
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(bookingRepository.existsByItemIdAndBookerIdAndStatusAndEndBefore(
                eq(item.getId()), eq(booker.getId()), eq(BookingStatus.APPROVED), any()))
                .thenReturn(true);
        when(commentMapper.toComment(commentDto, item.getId(), booker.getId())).thenReturn(comment);
        when(commentRepository.save(comment)).thenReturn(comment);
        when(commentMapper.toCommentDto(comment)).thenReturn(commentDto);

        CommentDto result = itemService.addComment(commentDto, booker.getId(), item.getId());
        assertEquals(commentDto.getText(), result.getText());
    }

    @Test
    void addComment_WithoutBooking_Throws() {
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(bookingRepository.existsByItemIdAndBookerIdAndStatusAndEndBefore(
                eq(item.getId()), eq(booker.getId()), eq(BookingStatus.APPROVED), any()))
                .thenReturn(false);

        assertThrows(ResponseStatusException.class,
                () -> itemService.addComment(commentDto, booker.getId(), item.getId()));
    }

    @Test
    void addComment_ItemNotFound() {
        when(itemRepository.findById(item.getId())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class,
                () -> itemService.addComment(commentDto, booker.getId(), item.getId()));
    }
}
