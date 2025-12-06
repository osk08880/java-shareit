package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.DuplicateEmailException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).name("Alice").email("alice@mail.com").build();
        userDto = UserDto.builder().id(1L).name("Alice").email("alice@mail.com").build();
    }

    @Test
    void createUser_success() {
        when(userRepository.findByEmail(userDto.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto result = userService.create(userDto);

        assertNotNull(result);
        assertEquals(userDto.getId(), result.getId());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUser_duplicateEmail_throwsException() {
        when(userRepository.findByEmail(userDto.getEmail())).thenReturn(Optional.of(user));

        assertThrows(DuplicateEmailException.class, () -> userService.create(userDto));
    }

    @Test
    void createUser_invalidEmail_throwsException() {
        UserDto invalidEmailUser = UserDto.builder().name("Bob").email("invalidEmail").build();
        assertThrows(IllegalArgumentException.class, () -> userService.create(invalidEmailUser));
    }

    @Test
    void updateUser_success() {
        UserDto updatedDto = UserDto.builder().name("Bob").email("bob@mail.com").build();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.findByEmail(updatedDto.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserDto result = userService.update(user.getId(), updatedDto);

        assertEquals("Bob", result.getName());
        assertEquals("bob@mail.com", result.getEmail());
    }

    @Test
    void updateUser_onlyNameUpdated() {
        UserDto updatedDto = UserDto.builder().name("New Name").build();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserDto result = userService.update(user.getId(), updatedDto);

        assertEquals("New Name", result.getName());
        assertEquals(user.getEmail(), result.getEmail());
    }

    @Test
    void updateUser_onlyEmailUpdated() {
        UserDto updatedDto = UserDto.builder().email("new@mail.com").build();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("new@mail.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserDto result = userService.update(user.getId(), updatedDto);

        assertEquals("new@mail.com", result.getEmail());
        assertEquals(user.getName(), result.getName());
    }

    @Test
    void updateUser_notFound_throwsException() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> userService.update(2L, userDto));
    }

    @Test
    void updateUser_duplicateEmail_throwsException() {
        UserDto updatedDto = UserDto.builder().email("bob@mail.com").build();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("bob@mail.com")).thenReturn(Optional.of(new User()));

        assertThrows(DuplicateEmailException.class, () -> userService.update(user.getId(), updatedDto));
    }

    @Test
    void findById_success() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        UserDto result = userService.findById(user.getId());

        assertEquals(user.getId(), result.getId());
    }

    @Test
    void findById_notFound_throwsException() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> userService.findById(2L));
    }

    @Test
    void findAll_returnsUsers() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(user));

        List<UserDto> result = userService.findAll();

        assertEquals(1, result.size());
        assertEquals(user.getId(), result.get(0).getId());
    }

    @Test
    void delete_success() {
        when(userRepository.existsById(user.getId())).thenReturn(true);
        doNothing().when(userRepository).deleteById(user.getId());

        userService.delete(user.getId());

        verify(userRepository, times(1)).deleteById(user.getId());
    }

    @Test
    void delete_notFound_throwsException() {
        when(userRepository.existsById(2L)).thenReturn(false);
        assertThrows(NotFoundException.class, () -> userService.delete(2L));
    }
}
