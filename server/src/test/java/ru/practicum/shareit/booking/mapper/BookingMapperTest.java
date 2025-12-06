package ru.practicum.shareit.booking.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoForItem;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.MappingException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BookingMapperTest {

    private BookingMapper mapper;
    private User booker;
    private Item item;
    private Booking booking;
    private BookingDto dto;

    @BeforeEach
    void setup() {
        mapper = new BookingMapper();

        booker = User.builder().id(1L).name("Alice").email("alice@example.com").build();
        item = Item.builder().id(2L).name("Drill").description("Powerful").available(true).owner(booker).build();

        booking = Booking.builder()
                .id(10L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build();

        dto = BookingDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .itemId(item.getId())
                .bookerId(booker.getId())
                .status(BookingStatus.APPROVED)
                .build();
    }

    @Test
    void toBookingDto_shouldMapCorrectly() {
        BookingDto mapped = mapper.toBookingDto(booking);

        assertThat(mapped).isNotNull();
        assertThat(mapped.getId()).isEqualTo(booking.getId());
        assertThat(mapped.getStart()).isEqualTo(booking.getStart());
        assertThat(mapped.getEnd()).isEqualTo(booking.getEnd());
        assertThat(mapped.getItemId()).isEqualTo(item.getId());
        assertThat(mapped.getBookerId()).isEqualTo(booker.getId());
        assertThat(mapped.getStatus()).isEqualTo(BookingStatus.APPROVED);
        assertThat(mapped.getItem()).isNotNull();
        assertThat(mapped.getBooker()).isNotNull();
    }

    @Test
    void toBookingDto_nullBooking_throwsException() {
        assertThatThrownBy(() -> mapper.toBookingDto(null))
                .isInstanceOf(MappingException.class)
                .hasMessageContaining("Booking для преобразования в DTO не может быть null");
    }

    @Test
    void toBookingDto_partialData_shouldHandleNulls() {
        Booking b = Booking.builder().id(20L).build();
        BookingDto mapped = mapper.toBookingDto(b);

        assertThat(mapped).isNotNull();
        assertThat(mapped.getItem()).isNull();
        assertThat(mapped.getBooker()).isNull();
        assertThat(mapped.getItemId()).isNull();
        assertThat(mapped.getBookerId()).isNull();
    }

    @Test
    void toBookingDtoForItem_shouldMapCorrectly() {
        BookingDtoForItem mapped = mapper.toBookingDtoForItem(booking);
        assertThat(mapped).isNotNull();
        assertThat(mapped.getId()).isEqualTo(booking.getId());
        assertThat(mapped.getStart()).isEqualTo(booking.getStart());
        assertThat(mapped.getEnd()).isEqualTo(booking.getEnd());
    }

    @Test
    void toBookingDtoForItem_nullBooking_returnsNull() {
        assertThat(mapper.toBookingDtoForItem(null)).isNull();
    }

    @Test
    void toBooking_shouldMapCorrectly() {
        Booking mapped = mapper.toBooking(dto, item, booker);

        assertThat(mapped).isNotNull();
        assertThat(mapped.getId()).isEqualTo(dto.getId());
        assertThat(mapped.getItem()).isEqualTo(item);
        assertThat(mapped.getBooker()).isEqualTo(booker);
        assertThat(mapped.getStatus()).isEqualTo(dto.getStatus());
    }

    @Test
    void toBooking_nullStatus_shouldDefaultToWaiting() {
        BookingDto dtoWithoutStatus = BookingDto.builder()
                .id(30L)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusHours(1))
                .itemId(item.getId())
                .bookerId(booker.getId())
                .build();

        Booking mapped = mapper.toBooking(dtoWithoutStatus, item, booker);
        assertThat(mapped.getStatus()).isEqualTo(BookingStatus.WAITING);
    }

    @Test
    void toBooking_nullDto_throwsException() {
        assertThatThrownBy(() -> mapper.toBooking(null, item, booker))
                .isInstanceOf(MappingException.class)
                .hasMessageContaining("BookingDto для преобразования в Booking не может быть null");
    }
}
