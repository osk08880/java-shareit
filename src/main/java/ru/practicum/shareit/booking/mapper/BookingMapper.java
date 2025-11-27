package ru.practicum.shareit.booking.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.MappingException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingMapper {

    public BookingDto toBookingDto(Booking booking) {
        if (booking == null) {
            log.warn("Попытка преобразовать null Booking в BookingDto");
            throw new MappingException("Booking для преобразования в DTO не может быть null");
        }

        ItemDto itemDto = booking.getItem() != null ? ItemDto.builder()
                .id(booking.getItem().getId())
                .name(booking.getItem().getName())
                .build() : null;

        UserDto bookerDto = booking.getBooker() != null ? UserDto.builder()
                .id(booking.getBooker().getId())
                .build() : null;

        return BookingDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .item(itemDto)
                .booker(bookerDto)
                .build();
    }

    public Booking toBooking(BookingDto dto, Item item, User booker) {
        if (dto == null) {
            log.warn("Попытка преобразовать null BookingDto в Booking");
            throw new MappingException("BookingDto для преобразования в Booking не может быть null");
        }

        return Booking.builder()
                .id(dto.getId())
                .start(dto.getStart())
                .end(dto.getEnd())
                .item(item)
                .booker(booker)
                .status(dto.getStatus() != null ? dto.getStatus() : BookingStatus.WAITING)
                .build();
    }
}
