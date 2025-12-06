package ru.practicum.shareit.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoForItem;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class DtoJsonExtendedTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void commentDto_fullTest() throws Exception {
        CommentDto comment = CommentDto.builder()
                .id(1L)
                .text("Nice item")
                .authorName("Bob")
                .created(LocalDateTime.of(2025, 12, 6, 12, 0, 0))
                .build();

        String json = objectMapper.writeValueAsString(comment);
        CommentDto deserialized = objectMapper.readValue(json, CommentDto.class);

        assertThat(deserialized.getId()).isEqualTo(1L);
        assertThat(deserialized.getText()).isEqualTo("Nice item");
        assertThat(deserialized.getAuthorName()).isEqualTo("Bob");
        assertThat(deserialized.getCreated()).isEqualTo(comment.getCreated());
    }

    @Test
    void itemDto_fullTest() throws Exception {
        ItemDto item = ItemDto.builder()
                .id(1L)
                .name("Drill")
                .description("Powerful drill")
                .available(true)
                .comments(Collections.singletonList(
                        CommentDto.builder()
                                .id(10L)
                                .text("Comment")
                                .authorName("Alice")
                                .created(LocalDateTime.now())
                                .build()
                ))
                .build();

        String json = objectMapper.writeValueAsString(item);
        ItemDto deserialized = objectMapper.readValue(json, ItemDto.class);

        assertThat(deserialized.getName()).isEqualTo("Drill");
        assertThat(deserialized.getComments()).hasSize(1);
        assertThat(deserialized.getComments().get(0).getAuthorName()).isEqualTo("Alice");
    }

    @Test
    void bookingDtoForItem_fullTest() throws Exception {
        BookingDtoForItem dto = BookingDtoForItem.builder()
                .id(1L)
                .bookerId(2L)
                .start(LocalDateTime.of(2025, 12, 6, 10, 0))
                .end(LocalDateTime.of(2025, 12, 6, 12, 0))
                .build();

        String json = objectMapper.writeValueAsString(dto);
        BookingDtoForItem deserialized = objectMapper.readValue(json, BookingDtoForItem.class);

        assertThat(deserialized.getBookerId()).isEqualTo(2L);
    }

    @Test
    void bookingDto_fullTest() throws Exception {
        BookingDto booking = BookingDto.builder()
                .id(1L)
                .status(BookingStatus.APPROVED)
                .start(LocalDateTime.of(2025, 12, 6, 10, 0))
                .end(LocalDateTime.of(2025, 12, 6, 12, 0))
                .bookerId(2L)
                .itemId(3L)
                .booker(UserDto.builder().id(2L).name("Alice").email("alice@example.com").build())
                .item(ItemDto.builder().id(3L).name("Drill").description("Powerful drill").available(true).build())
                .build();

        String json = objectMapper.writeValueAsString(booking);
        BookingDto deserialized = objectMapper.readValue(json, BookingDto.class);

        assertThat(deserialized.getStatus()).isEqualTo(BookingStatus.APPROVED);
        assertThat(deserialized.getBooker().getName()).isEqualTo("Alice");
        assertThat(deserialized.getItem().getName()).isEqualTo("Drill");
    }

    @Test
    void itemRequestDto_fullTest() throws Exception {
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .id(1L)
                .description("Need drill")
                .created(LocalDateTime.of(2025, 12, 6, 15, 0))
                .requestor(UserDto.builder().id(1L).name("Bob").email("bob@example.com").build())
                .items(Collections.singletonList(
                        ItemDto.builder().id(10L).name("Drill").description("Powerful").available(true).build()
                ))
                .build();

        String json = objectMapper.writeValueAsString(requestDto);
        ItemRequestDto deserialized = objectMapper.readValue(json, ItemRequestDto.class);

        assertThat(deserialized.getDescription()).isEqualTo("Need drill");
        assertThat(deserialized.getRequestor().getName()).isEqualTo("Bob");
        assertThat(deserialized.getItems()).hasSize(1);
        assertThat(deserialized.getItems().get(0).getName()).isEqualTo("Drill");
    }

    @Test
    void userDto_fullTest() throws Exception {
        UserDto user = UserDto.builder()
                .id(1L)
                .name("Alice")
                .email("alice@example.com")
                .build();

        String json = objectMapper.writeValueAsString(user);
        UserDto deserialized = objectMapper.readValue(json, UserDto.class);

        assertThat(deserialized.getEmail()).isEqualTo("alice@example.com");
        assertThat(deserialized.getName()).isEqualTo("Alice");
    }
}
