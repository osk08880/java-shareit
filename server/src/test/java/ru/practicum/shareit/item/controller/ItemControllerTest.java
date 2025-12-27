package ru.practicum.shareit.item.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    @Test
    void updateItem_success() throws Exception {
        ItemDto dto = ItemDto.builder()
                .id(1L)
                .name("Updated Drill")
                .description("Updated description")
                .available(false)
                .build();

        when(itemService.update(anyLong(), anyLong(), any(ItemDto.class))).thenReturn(dto);

        mockMvc.perform(patch("/items/{itemId}", 1)
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(dto.getId()))
                .andExpect(jsonPath("$.name").value(dto.getName()))
                .andExpect(jsonPath("$.description").value(dto.getDescription()))
                .andExpect(jsonPath("$.available").value(dto.getAvailable()));
    }

    @Test
    void getItemById_success() throws Exception {
        ItemDto dto = ItemDto.builder()
                .id(1L)
                .name("Drill")
                .description("Powerful drill")
                .available(true)
                .build();

        when(itemService.findById(anyLong(), anyLong())).thenReturn(dto);

        mockMvc.perform(get("/items/{itemId}", 1)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(dto.getId()))
                .andExpect(jsonPath("$.name").value(dto.getName()))
                .andExpect(jsonPath("$.description").value(dto.getDescription()))
                .andExpect(jsonPath("$.available").value(dto.getAvailable()));
    }

    @Test
    void findAllByOwner_success() throws Exception {
        ItemDto dto = ItemDto.builder()
                .id(1L)
                .name("Drill")
                .description("Powerful drill")
                .available(true)
                .build();

        when(itemService.findAllByOwner(anyLong())).thenReturn(Collections.singletonList(dto));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(dto.getId()))
                .andExpect(jsonPath("$[0].name").value(dto.getName()));
    }

    @Test
    void searchItems_success() throws Exception {
        ItemDto dto = ItemDto.builder()
                .id(1L)
                .name("Drill")
                .description("Powerful drill")
                .available(true)
                .build();

        when(itemService.search(anyString())).thenReturn(Collections.singletonList(dto));

        mockMvc.perform(get("/items/search")
                        .param("text", "drill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(dto.getId()))
                .andExpect(jsonPath("$[0].name").value(dto.getName()));
    }

    @Test
    void addComment_success() throws Exception {
        CommentDto comment = CommentDto.builder()
                .id(1L)
                .text("Nice item")
                .authorName("Bob")
                .created(LocalDateTime.now())
                .build();

        when(itemService.addComment(any(CommentDto.class), anyLong(), anyLong())).thenReturn(comment);

        mockMvc.perform(post("/items/{itemId}/comment", 1)
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(comment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(comment.getId()))
                .andExpect(jsonPath("$.text").value(comment.getText()))
                .andExpect(jsonPath("$.authorName").value(comment.getAuthorName()));
    }
}
