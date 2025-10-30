package ru.practicum.shareit.user.mapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.MappingException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.dto.UserDto;

@Slf4j
@Component
public class UserMapper {

    public static UserDto toUserDto(User user) {
        if (user == null) {
            throw new MappingException("User для преобразования в UserDto не может быть null");
        }
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public static User toUser(UserDto userDto) {
        if (userDto == null) {
            throw new MappingException("UserDto для преобразования в User не может быть null");
        }
        return User.builder()
                .id(userDto.getId())
                .name(userDto.getName())
                .email(userDto.getEmail())
                .build();
    }
}
