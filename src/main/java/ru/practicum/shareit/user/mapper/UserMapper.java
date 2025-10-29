package ru.practicum.shareit.user.mapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.dto.UserDto;

@Slf4j
@Component
public class UserMapper {

    public static UserDto toUserDto(User user) {
        if (user == null) {
            log.warn("Попытка преобразовать null User в UserDto");
            return null;
        }
        log.info("Преобразование User в UserDto: {}", user);
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public static User toUser(UserDto userDto) {
        if (userDto == null) {
            log.warn("Попытка преобразовать null UserDto в User");
            return null;
        }
        log.info("Преобразование UserDto в User: {}", userDto);
        return User.builder()
                .id(userDto.getId())
                .name(userDto.getName())
                .email(userDto.getEmail())
                .build();
    }
}
