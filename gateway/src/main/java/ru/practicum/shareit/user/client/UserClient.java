package ru.practicum.shareit.user.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.user.dto.UserDto;

@Slf4j
@Component
public class UserClient extends BaseClient {

    private static final String API_PREFIX = "/users";

    public UserClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory("http://shareit-server:9090" + API_PREFIX))
                        .build()
        );
        log.info("UserClient initialized with server URL: {}", serverUrl + API_PREFIX);
    }

    public ResponseEntity<Object> create(UserDto userDto) {
        return post("", userDto);
    }

    public ResponseEntity<Object> findAll() {
        return get("");
    }

    public ResponseEntity<Object> findById(Long id) {
        return get("/" + id);
    }

    public ResponseEntity<Object> update(Long id, UserDto userDto) {
        return patch("/" + id, userDto);
    }

    public ResponseEntity<Object> delete(Long id) {
        return delete("/" + id);
    }
}
