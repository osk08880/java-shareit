package ru.practicum.shareit.user.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.exception.MappingException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    @Test
    void toUserDto_shouldMapCorrectly() {
        User user = User.builder().id(1L).name("Alice").email("alice@example.com").build();
        UserDto dto = UserMapper.toUserDto(user);

        assertEquals(user.getId(), dto.getId());
        assertEquals(user.getName(), dto.getName());
        assertEquals(user.getEmail(), dto.getEmail());
    }

    @Test
    void toUserDto_nullUser_throwsException() {
        assertThrows(MappingException.class, () -> UserMapper.toUserDto(null));
    }

    @Test
    void toUser_shouldMapCorrectly() {
        UserDto dto = UserDto.builder().id(2L).name("Bob").email("bob@example.com").build();
        User user = UserMapper.toUser(dto);

        assertEquals(dto.getId(), user.getId());
        assertEquals(dto.getName(), user.getName());
        assertEquals(dto.getEmail(), user.getEmail());
    }

    @Test
    void toUser_nullDto_throwsException() {
        assertThrows(MappingException.class, () -> UserMapper.toUser(null));
    }

    @Test
    void toUser_withNullFields_shouldMapCorrectly() {
        UserDto dto = UserDto.builder().id(3L).name(null).email(null).build();
        User user = UserMapper.toUser(dto);

        assertEquals(3L, user.getId());
        assertNull(user.getName());
        assertNull(user.getEmail());
    }

    @Test
    void toUserDto_withNullFields_shouldMapCorrectly() {
        User user = User.builder().id(4L).name(null).email(null).build();
        UserDto dto = UserMapper.toUserDto(user);

        assertEquals(4L, dto.getId());
        assertNull(dto.getName());
        assertNull(dto.getEmail());
    }
}
