package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ItemRequestServiceTest {

    @InjectMocks
    private ItemRequestService service;

    @Mock
    private ItemRequestRepository requestRepository;

    @Mock
    private UserRepository userRepository;

    private User user;
    private ItemRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = User.builder()
                .id(1L)
                .name("Alice")
                .email("alice@example.com")
                .build();

        request = ItemRequest.builder()
                .id(10L)
                .description("Need drill")
                .requestor(user)
                .created(LocalDateTime.now())
                .build();
    }

    @Test
    void createRequest_success() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(requestRepository.save(any(ItemRequest.class))).thenReturn(request);

        ItemRequestDto dto = ItemRequestDto.builder()
                .description("Need drill")
                .build();

        ItemRequestDto result = service.create(dto, user.getId());

        assertNotNull(result);
        assertEquals(request.getId(), result.getId());
        assertEquals(user.getId(), result.getRequestor().getId());
        verify(requestRepository, times(1)).save(any(ItemRequest.class));
    }

    @Test
    void createRequest_userNotFound_throws() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        ItemRequestDto dto = ItemRequestDto.builder().description("Need drill").build();

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> service.create(dto, user.getId()));
        assertTrue(ex.getMessage().contains("Пользователь не найден"));
    }

    @Test
    void getOwnRequests_success() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(requestRepository.findAllByRequestorOrderByCreatedDesc(user))
                .thenReturn(List.of(request));

        List<ItemRequestDto> result = service.getOwnRequests(user.getId());

        assertEquals(1, result.size());
        assertEquals(request.getId(), result.get(0).getId());
    }

    @Test
    void getOwnRequests_userNotFound_throws() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> service.getOwnRequests(user.getId()));
        assertTrue(ex.getMessage().contains("Пользователь не найден"));
    }

    @Test
    void getAllRequests_success() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(requestRepository.findAllByRequestorIdNotOrderByCreatedDesc(user.getId()))
                .thenReturn(List.of(request));

        List<ItemRequestDto> result = service.getAllRequests(user.getId());

        assertEquals(1, result.size());
        assertEquals(request.getId(), result.get(0).getId());
    }

    @Test
    void getAllRequests_userNotFound_throws() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> service.getAllRequests(user.getId()));
        assertTrue(ex.getMessage().contains("Пользователь не найден"));
    }

    @Test
    void getRequestById_success() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));

        ItemRequestDto result = service.getRequestById(request.getId(), user.getId());

        assertNotNull(result);
        assertEquals(request.getId(), result.getId());
    }

    @Test
    void getRequestById_requestNotFound_throws() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(requestRepository.findById(request.getId())).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> service.getRequestById(request.getId(), user.getId()));
        assertTrue(ex.getMessage().contains("Запрос не найден"));
    }

    @Test
    void getRequestById_userNotFound_throws() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> service.getRequestById(request.getId(), user.getId()));
        assertTrue(ex.getMessage().contains("Пользователь не найден"));
    }
}
