package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.storage.InMemoryItemHistoryStorage;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.InMemoryItemRequestStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final InMemoryItemRequestStorage requestStorage;
    private final InMemoryItemHistoryStorage historyStorage;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final BookingMapper bookingMapper;

    @Override
    public ItemDto create(Long userId, ItemDto itemDto, Long requestId) {
        log.debug("Создание вещи: userId={}, itemDto={}, requestId={}", userId, itemDto, requestId);

        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        ItemRequest request = null;
        if (requestId != null) {
            request = requestStorage.findById(requestId)
                    .orElseThrow(() -> new NotFoundException("Запрос не найден"));
        }

        Boolean available = Boolean.TRUE.equals(itemDto.getAvailable());
        Item item = ItemMapper.toItem(itemDto, owner.getId(), request != null ? request.getId() : null);
        item.setAvailable(available);

        Item savedItem = itemRepository.save(item);
        log.info("Вещь создана пользователем {}: {}", userId, savedItem);

        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        if (!existingItem.getOwnerId().equals(userId)) {
            throw new NotFoundException("Только владелец может обновить вещь");
        }

        Boolean available = Boolean.TRUE.equals(itemDto.getAvailable());
        Item updatedItem = Item.builder()
                .id(itemId)
                .name(itemDto.getName() != null ? itemDto.getName().trim() : existingItem.getName())
                .description(itemDto.getDescription() != null ? itemDto.getDescription().trim() : existingItem.getDescription())
                .available(available)
                .ownerId(existingItem.getOwnerId())
                .requestId(existingItem.getRequestId())
                .build();

        Item savedItem = itemRepository.save(updatedItem);
        log.info("Вещь {} обновлена пользователем {}", itemId, userId);

        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    public ItemDto findById(Long userId, Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Предмет с ID " + itemId + " не найден"));

        ItemDto dto = ItemMapper.toItemDto(item);

        if (item.getOwnerId().equals(userId)) {
            enrichWithBookings(dto, itemId);
        } else {
            dto.setLastBooking(null);
            dto.setNextBooking(null);
        }

        List<CommentDto> comments = commentRepository.findAllByItemIdOrderByCreatedDesc(itemId)
                .stream()
                .map(commentMapper::toCommentDto)
                .collect(Collectors.toList());
        dto.setComments(comments);

        historyStorage.addView(userId, itemId);

        return dto;
    }

    @Override
    public List<ItemDto> findAllByOwner(Long userId) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        List<ItemDto> items = itemRepository.findByOwnerId(userId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());

        items.forEach(dto -> {
            enrichWithBookings(dto, dto.getId());
            List<CommentDto> comments = commentRepository.findAllByItemIdOrderByCreatedDesc(dto.getId())
                    .stream()
                    .map(commentMapper::toCommentDto)
                    .collect(Collectors.toList());
            dto.setComments(comments);
        });

        return items;
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) return Collections.emptyList();

        return itemRepository.searchAvailableItems(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    private void enrichWithBookings(ItemDto itemDto, Long itemId) {
        LocalDateTime now = LocalDateTime.now();

        bookingRepository.findFirstByItemIdAndStatusAndEndBeforeOrderByEndDesc(itemId, BookingStatus.APPROVED, now)
                .ifPresentOrElse(
                        lastBooking -> itemDto.setLastBooking(bookingMapper.toBookingDto(lastBooking)),
                        () -> itemDto.setLastBooking(null)
                );

        bookingRepository.findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(itemId, BookingStatus.APPROVED, now)
                .ifPresentOrElse(
                        nextBooking -> itemDto.setNextBooking(bookingMapper.toBookingDto(nextBooking)),
                        () -> itemDto.setNextBooking(null)
                );
    }

    @Override
    public CommentDto addComment(CommentDto dto, Long userId, Long itemId) {
        itemRepository.findById(itemId).orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        LocalDateTime now = LocalDateTime.now();

        boolean hasBooking = bookingRepository.existsByItemIdAndBookerIdAndStatusAndEndBefore(itemId, userId, BookingStatus.APPROVED, now);

        if (!hasBooking) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Только тот, кто арендовал, может оставить отзыв");
        }

        Comment comment = commentMapper.toComment(dto, itemId, userId);
        Comment saved = commentRepository.save(comment);

        return commentMapper.toCommentDto(saved);
    }
}
