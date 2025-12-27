package ru.practicum.shareit.request.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemRequestService itemRequestService;

    @Autowired
    private ObjectMapper objectMapper;

    private ItemRequestDto requestDto;

    @BeforeEach
    void setUp() {
        requestDto = ItemRequestDto.builder()
                .id(1L)
                .description("Need a drill")
                .created(LocalDateTime.now())
                .items(Collections.emptyList())
                .build();
    }

    @Test
    void createRequest_success() throws Exception {
        Mockito.when(itemRequestService.create(Mockito.any(ItemRequestDto.class), Mockito.anyLong()))
                .thenReturn(requestDto);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(requestDto.getId().intValue())))
                .andExpect(jsonPath("$.description", is(requestDto.getDescription())));
    }

    @Test
    void getOwnRequests_success() throws Exception {
        List<ItemRequestDto> list = Collections.singletonList(requestDto);
        Mockito.when(itemRequestService.getOwnRequests(Mockito.anyLong()))
                .thenReturn(list);

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(requestDto.getId().intValue())));
    }

    @Test
    void getAllRequests_success() throws Exception {
        List<ItemRequestDto> list = Collections.singletonList(requestDto);
        Mockito.when(itemRequestService.getAllRequests(Mockito.anyLong()))
                .thenReturn(list);

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(requestDto.getId().intValue())));
    }

    @Test
    void getRequestById_success() throws Exception {
        Mockito.when(itemRequestService.getRequestById(Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(requestDto);

        mockMvc.perform(get("/requests/{requestId}", 1L)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(requestDto.getId().intValue())))
                .andExpect(jsonPath("$.description", is(requestDto.getDescription())));
    }
}
