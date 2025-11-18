package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {

    ItemDto create(Long userId, ItemDto itemDto, Long requestId);

    ItemDto update(Long userId, Long itemId, ItemDto itemDto);

    ItemDto findById(Long userId, Long itemId);

    List<ItemDto> findAllByOwner(Long userId);

    List<ItemDto> search(String text);

    CommentDto addComment(CommentDto dto, Long userId, Long itemId);
}