package ru.practicum.shareit.user.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.client.BaseClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class UserClient extends BaseClient {

    private static final String BASE_PATH = "/users";

    public UserClient(RestTemplateBuilder builder) {
        super(builder.build());
    }

    public ResponseEntity<Object> create(UserDto userDto) {
        return post(BASE_PATH, userDto, Map.of(), Object.class, null, null);
    }

    public ResponseEntity<List<UserDto>> findAll() {
        return get(BASE_PATH, Map.of(), new ParameterizedTypeReference<>() {}, null, null);
    }

    public ResponseEntity<Object> findById(Long id) {
        return get(BASE_PATH + "/{id}", Map.of("id", id), Object.class, null, null);
    }

    public ResponseEntity<Object> update(Long id, UserDto userDto) {
        return patch(BASE_PATH + "/{id}", userDto, Map.of("id", id), Object.class, null, null);
    }

    public ResponseEntity<Object> delete(Long id) {
        return delete(BASE_PATH + "/{id}", Map.of("id", id), Object.class, null, null);
    }
}
