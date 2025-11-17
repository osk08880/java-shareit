package ru.practicum.shareit.booking.mapper;

import lombok.extern.slf4j.Slf4j;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.MappingException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

@Slf4j
public class BookingMapper {

    public static BookingDto toBookingDto(Booking booking) {
        if (booking == null) {
            throw new MappingException("Booking для преобразования в DTO не может быть null");
        }

        BookingDto dto = BookingDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .itemId(booking.getItem() != null ? booking.getItem().getId() : null)
                .bookerId(booking.getBooker() != null ? booking.getBooker().getId() : null)
                .status(booking.getStatus() != null ? booking.getStatus().name() : null)
                .build();

        log.info("Бронирование преобразовано в DTO: {}", dto);
        return dto;
    }

    public static Booking toBooking(BookingDto dto, Item item, User booker) {
        if (dto == null) {
            throw new MappingException("BookingDto для преобразования в Booking не может быть null");
        }

        return Booking.builder()
                .id(dto.getId())
                .start(dto.getStart())
                .end(dto.getEnd())
                .item(item)
                .booker(booker)
                .status(dto.getStatus() != null ? BookingStatus.valueOf(dto.getStatus()) : BookingStatus.WAITING)
                .build();
    }
}
