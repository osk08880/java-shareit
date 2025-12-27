package ru.practicum.shareit.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.State;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @Autowired
    private ObjectMapper objectMapper;

    private BookingDto bookingDto;

    @BeforeEach
    void setUp() {
        bookingDto = BookingDto.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.WAITING)
                .itemId(10L)
                .item(ItemDto.builder().id(10L).name("Drill").available(true).build())
                .bookerId(100L)
                .booker(UserDto.builder().id(100L).name("Alice").email("alice@example.com").build())
                .build();
    }

    @Test
    void createBooking_success() throws Exception {
        Mockito.when(bookingService.create(any(BookingDto.class), anyLong()))
                .thenReturn(bookingDto);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 100L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId().intValue())))
                .andExpect(jsonPath("$.item.id", is(bookingDto.getItemId().intValue())))
                .andExpect(jsonPath("$.booker.id", is(bookingDto.getBookerId().intValue())));
    }

    @Test
    void approveBooking_success() throws Exception {
        bookingDto.setStatus(BookingStatus.APPROVED);
        Mockito.when(bookingService.approve(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(bookingDto);

        mockMvc.perform(patch("/bookings/{bookingId}", 1)
                        .param("approved", "true")
                        .header("X-Sharer-User-Id", 100L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(BookingStatus.APPROVED.toString())));
    }

    @Test
    void getBookingById_success() throws Exception {
        Mockito.when(bookingService.getById(anyLong(), anyLong()))
                .thenReturn(bookingDto);

        mockMvc.perform(get("/bookings/{bookingId}", 1)
                        .header("X-Sharer-User-Id", 100L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId().intValue())))
                .andExpect(jsonPath("$.booker.id", is(bookingDto.getBookerId().intValue())));
    }

    @Test
    void getAllBookings_success() throws Exception {
        List<BookingDto> bookings = Collections.singletonList(bookingDto);
        Mockito.when(bookingService.getAll(any(State.class), anyLong()))
                .thenReturn(bookings);

        mockMvc.perform(get("/bookings")
                        .param("state", "ALL")
                        .header("X-Sharer-User-Id", 100L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(bookingDto.getId().intValue())));
    }

    @Test
    void getOwnerAllBookings_success() throws Exception {
        List<BookingDto> bookings = Collections.singletonList(bookingDto);
        Mockito.when(bookingService.getOwnerAll(any(State.class), anyLong()))
                .thenReturn(bookings);

        mockMvc.perform(get("/bookings/owner")
                        .param("state", "ALL")
                        .header("X-Sharer-User-Id", 100L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(bookingDto.getId().intValue())));
    }
}
