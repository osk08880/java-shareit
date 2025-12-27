package ru.practicum.shareit.request.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ItemRequestMapperTest {

    @Test
    void toItemRequest_shouldMapCorrectly() {
        User requestor = User.builder().id(1L).name("Alice").email("a@a.com").build();
        ItemRequestDto dto = ItemRequestDto.builder()
                .id(10L)
                .description("Need drill")
                .created(LocalDateTime.now())
                .build();

        ItemRequest request = ItemRequestMapper.toItemRequest(dto, requestor);

        assertThat(request).isNotNull();
        assertThat(request.getId()).isEqualTo(dto.getId());
        assertThat(request.getDescription()).isEqualTo(dto.getDescription());
        assertThat(request.getRequestor()).isEqualTo(requestor);
        assertThat(request.getCreated()).isNotNull();
    }

    @Test
    void toItemRequest_shouldThrowOnNullDto() {
        User requestor = User.builder().id(1L).build();
        assertThatThrownBy(() -> ItemRequestMapper.toItemRequest(null, requestor))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ItemRequestDto не может быть null");
    }

    @Test
    void toItemRequestDto_shouldMapCorrectly_withItems() {
        User requestor = User.builder().id(1L).name("Alice").email("a@a.com").build();
        ItemRequest request = ItemRequest.builder()
                .id(10L)
                .description("Need drill")
                .requestor(requestor)
                .created(LocalDateTime.now())
                .items(Collections.singletonList(Item.builder().id(5L).name("Drill").build()))
                .build();

        ItemRequestDto dto = ItemRequestMapper.toItemRequestDto(request);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(request.getId());
        assertThat(dto.getDescription()).isEqualTo(request.getDescription());
        assertThat(dto.getRequestor().getId()).isEqualTo(requestor.getId());
        assertThat(dto.getItems()).hasSize(1);
        assertThat(dto.getItems().get(0).getId()).isEqualTo(5L);
    }

    @Test
    void toItemRequestDto_shouldReturnEmptyItems_whenIncludeItemsFalse() {
        User requestor = User.builder().id(1L).name("Alice").email("a@a.com").build();
        ItemRequest request = ItemRequest.builder()
                .id(10L)
                .description("Need drill")
                .requestor(requestor)
                .created(LocalDateTime.now())
                .items(Collections.singletonList(Item.builder().id(5L).name("Drill").build()))
                .build();

        ItemRequestDto dto = ItemRequestMapper.toDto(request, false);

        assertThat(dto).isNotNull();
        assertThat(dto.getItems()).isEmpty();
    }

    @Test
    void toItemRequestDto_shouldHandleNullItemsList() {
        User requestor = User.builder().id(1L).name("Alice").email("a@a.com").build();
        ItemRequest request = ItemRequest.builder()
                .id(10L)
                .description("Need drill")
                .requestor(requestor)
                .created(LocalDateTime.now())
                .items(null)
                .build();

        ItemRequestDto dto = ItemRequestMapper.toDto(request, true);

        assertThat(dto).isNotNull();
        assertThat(dto.getItems()).isEmpty();
    }

    @Test
    void toItemRequestDto_shouldReturnNull_whenRequestNull() {
        assertThat(ItemRequestMapper.toItemRequestDto(null)).isNull();
        assertThat(ItemRequestMapper.toDto(null, true)).isNull();
    }
}
