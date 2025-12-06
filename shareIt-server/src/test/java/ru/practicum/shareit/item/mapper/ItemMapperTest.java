package ru.practicum.shareit.item.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import static org.junit.jupiter.api.Assertions.*;

class ItemMapperTest {

    @Test
    void toItemDto_withRequestAndOwner_shouldMapCorrectly() {
        User owner = User.builder()
                .id(1L)
                .name("Alice")
                .email("alice@example.com")
                .build();

        User requestor = User.builder()
                .id(2L)
                .name("Bob")
                .email("bob@example.com")
                .build();

        ItemRequest request = ItemRequest.builder()
                .id(10L)
                .description("Need a drill")
                .requestor(requestor)
                .build();

        Item item = Item.builder()
                .id(5L)
                .name("Drill")
                .description("Powerful drill")
                .available(true)
                .owner(owner)
                .request(request)
                .build();

        ItemDto dto = ItemMapper.toItemDto(item, true);

        assertEquals(item.getId(), dto.getId());
        assertEquals(item.getName(), dto.getName());
        assertEquals(item.getDescription(), dto.getDescription());
        assertEquals(item.getOwner().getId(), dto.getOwner().getId());
        assertEquals(item.getRequest().getId(), dto.getRequest().getId());
    }

    @Test
    void toItemDto_nullItem_returnsNull() {
        assertNull(ItemMapper.toItemDto(null, true));
    }

    @Test
    void toItem_shouldMapCorrectly() {
        User owner = User.builder().id(1L).name("Alice").email("a@a.com").build();
        ItemRequest request = ItemRequest.builder().id(2L).description("Request").build();
        ItemDto dto = ItemDto.builder().id(3L).name("Saw").description("Electric").available(true).build();

        Item item = ItemMapper.toItem(dto, owner, request);

        assertEquals(dto.getId(), item.getId());
        assertEquals(dto.getName(), item.getName());
        assertEquals(dto.getDescription(), item.getDescription());
        assertEquals(dto.getAvailable(), item.getAvailable());
        assertEquals(owner, item.getOwner());
        assertEquals(request, item.getRequest());
    }

    @Test
    void toItem_nullDto_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> ItemMapper.toItem(null, new User(), null));
    }

    @Test
    void toItemDto_nullRequest_shouldMapCorrectly() {
        User owner = User.builder().id(1L).name("Alice").email("a@a.com").build();
        Item item = Item.builder().id(5L).name("Drill").description("Powerful drill").available(true)
                .owner(owner)
                .request(null)
                .build();

        ItemDto dto = ItemMapper.toItemDto(item, true);

        assertEquals(item.getId(), dto.getId());
        assertEquals(item.getName(), dto.getName());
        assertEquals(item.getDescription(), dto.getDescription());
        assertEquals(item.getOwner().getId(), dto.getOwner().getId());
        assertNull(dto.getRequest());
        assertTrue(dto.getAvailable());
    }
}
