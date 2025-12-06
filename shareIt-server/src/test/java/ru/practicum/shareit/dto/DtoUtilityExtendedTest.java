package ru.practicum.shareit.dto;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingDtoForItem;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class DtoUtilityExtendedTest {

    @Test
    void commentDto_fallback_createsCommentWithDefaults() {
        var comment = ru.practicum.shareit.item.dto.CommentDto.fallback(null, null);

        assertThat(comment.getId()).isNull();
        assertThat(comment.getText()).isEqualTo("");
        assertThat(comment.getAuthorName()).isEqualTo("");
        assertThat(comment.getCreated()).isNotNull();
    }

    @Test
    void toItemDto_mapsItemCorrectly() {
        User owner = new User();
        owner.setId(1L);
        owner.setName("Owner");

        Item item = new Item();
        item.setId(10L);
        item.setName("Drill");
        item.setDescription("Powerful drill");
        item.setAvailable(true);
        item.setOwner(owner);

        var dto = ru.practicum.shareit.item.dto.ItemDto.toItemDto(item);

        assertThat(dto.getId()).isEqualTo(item.getId());
        assertThat(dto.getName()).isEqualTo(item.getName());
        assertThat(dto.getDescription()).isEqualTo(item.getDescription());
        assertThat(dto.getAvailable()).isTrue();
        assertThat(dto.getOwner().getId()).isEqualTo(owner.getId());
        assertThat(dto.getComments()).isEmpty();
    }

    @Test
    void toItemDto_nullItem_returnsNull() {
        var dto = ru.practicum.shareit.item.dto.ItemDto.toItemDto(null);
        assertThat(dto).isNull();
    }

    @Test
    void bookingDtoForItem_toDto_mapsCorrectly() {
        Booking booking = new Booking();
        booking.setId(100L);
        booking.setBooker(new User(){{
            setId(2L);
        }});
        booking.setStart(LocalDateTime.of(2025,12,6,10,0));
        booking.setEnd(LocalDateTime.of(2025,12,6,12,0));

        var dto = BookingDtoForItem.toDto(booking);

        assertThat(dto.getId()).isEqualTo(100L);
        assertThat(dto.getBookerId()).isEqualTo(2L);
        assertThat(dto.getStart()).isEqualTo(booking.getStart());
        assertThat(dto.getEnd()).isEqualTo(booking.getEnd());
    }

    @Test
    void itemRequestDto_toDto_mapsCorrectly() {
        User requestor = new User();
        requestor.setId(1L);
        requestor.setName("Bob");

        ItemRequest request = new ItemRequest();
        request.setId(50L);
        request.setDescription("Need drill");
        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.of(2025,12,6,15,0));

        var dto = ItemRequestDto.toDto(request, true);

        assertThat(dto.getId()).isEqualTo(50L);
        assertThat(dto.getDescription()).isEqualTo("Need drill");
        assertThat(dto.getRequestor().getId()).isEqualTo(1L);
    }

    @Test
    void itemRequestDto_toDto_nullRequest_returnsNull() {
        var dto = ItemRequestDto.toDto(null, true);
        assertThat(dto).isNull();
    }
}
