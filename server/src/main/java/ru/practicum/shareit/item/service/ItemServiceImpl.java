package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.dto.BookingDtoForItem;
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
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {


    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemRequestRepository requestRepository;
    private final InMemoryItemHistoryStorage historyStorage;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;

    private static final String MSG_USER_NOT_FOUND = "Пользователь не найден: ";
    private static final String MSG_ITEM_NOT_FOUND = "Вещь не найдена: ";
    private static final String MSG_REQUEST_NOT_FOUND = "Запрос не найден: ";
    private static final String MSG_ONLY_OWNER_CAN_UPDATE = "Только владелец может обновить вещь";
    private static final String MSG_COMMENT_FORBIDDEN = "Только арендатор может оставить отзыв";
    private static final String MSG_CANNOT_ANSWER_OWN_REQUEST = "Нельзя отвечать на свой запрос";

    @Override
    @Transactional
    public ItemDto create(Long userId, ItemDto itemDto, Long requestId) {
        log.info("SERVER: Создание вещи пользователем {}: {}", userId, itemDto);

        User owner = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn(MSG_USER_NOT_FOUND + userId);
                    return new NotFoundException(MSG_USER_NOT_FOUND + userId);
                });

        ItemRequest request = null;
        if (requestId != null) {
            request = requestRepository.findById(requestId)
                    .orElseThrow(() -> {
                        log.warn(MSG_REQUEST_NOT_FOUND + requestId);
                        return new NotFoundException(MSG_REQUEST_NOT_FOUND + requestId);
                    });
            if (request.getRequestor().getId().equals(userId)) {
                log.warn(MSG_CANNOT_ANSWER_OWN_REQUEST);
                throw new IllegalArgumentException(MSG_CANNOT_ANSWER_OWN_REQUEST);
            }
        }

        Item saved = itemRepository.save(ItemMapper.toItem(itemDto, owner, request));
        ItemDto dto = ItemMapper.toItemDto(saved, true);

        log.info("SERVER: Вещь создана: {}", dto);
        return dto;
    }

    @Override
    @Transactional
    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        log.info("SERVER: Обновление вещи {} пользователем {}: {}", itemId, userId, itemDto);

        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.warn(MSG_ITEM_NOT_FOUND + itemId);
                    return new NotFoundException(MSG_ITEM_NOT_FOUND + itemId);
                });

        if (!existingItem.getOwner().getId().equals(userId)) {
            log.warn(MSG_ONLY_OWNER_CAN_UPDATE);
            throw new NotFoundException(MSG_ONLY_OWNER_CAN_UPDATE);
        }

        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            existingItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            existingItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existingItem.setAvailable(itemDto.getAvailable());
        }

        Item savedItem = itemRepository.save(existingItem);
        ItemDto dto = ItemMapper.toItemDto(savedItem, true);
        enrichWithBookings(dto, itemId);

        log.info("SERVER: Вещь обновлена: {}", dto);
        return dto;
    }

    @Override
    public ItemDto findById(Long userId, Long itemId) {
        log.info("SERVER: Получение вещи {} пользователем {}", itemId, userId);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.warn(MSG_ITEM_NOT_FOUND + itemId);
                    return new NotFoundException(MSG_ITEM_NOT_FOUND + itemId);
                });

        ItemDto dto = ItemMapper.toItemDto(item, true);

        if (item.getOwner().getId().equals(userId)) {
            enrichWithBookings(dto, itemId);
        } else {
            dto.setLastBooking(null);
            dto.setNextBooking(null);
        }

        List<Comment> comments = commentRepository.findByItemIdOrderByCreatedDesc(itemId);
        dto.setComments(comments.stream()
                .map(commentMapper::toCommentDto)
                .collect(Collectors.toList()));

        historyStorage.addView(userId, itemId);

        log.info("SERVER: Получена вещь: {}", dto);
        return dto;
    }

    @Override
    public List<ItemDto> findAllByOwner(Long userId) {
        log.info("SERVER: Получение всех вещей пользователя {}", userId);

        userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn(MSG_USER_NOT_FOUND + userId);
                    return new NotFoundException(MSG_USER_NOT_FOUND + userId);
                });

        List<ItemDto> items = itemRepository.findByOwnerId(userId).stream()
                .map(item -> {
                    ItemDto dto = ItemMapper.toItemDto(item, true);
                    enrichWithBookings(dto, dto.getId());
                    return dto;
                })
                .collect(Collectors.toList());

        log.info("SERVER: Найдено {} вещей", items.size());
        return items;
    }

    @Override
    public List<ItemDto> search(String text) {
        log.info("SERVER: Поиск вещей по тексту '{}'", text);
        if (text == null || text.isBlank()) return Collections.emptyList();

        List<ItemDto> items = itemRepository.searchAvailableItems(text).stream()
                .map(item -> ItemMapper.toItemDto(item, true))
                .collect(Collectors.toList());

        log.info("SERVER: Найдено {} вещей", items.size());
        return items;
    }

    private void enrichWithBookings(ItemDto itemDto, Long itemId) {
        LocalDateTime now = LocalDateTime.now();

        bookingRepository.findFirstByItemIdAndStatusAndEndBeforeOrderByEndDesc(itemId, BookingStatus.APPROVED, now)
                .ifPresentOrElse(
                        lastBooking -> itemDto.setLastBooking(BookingDtoForItem.toDto(lastBooking)),
                        () -> itemDto.setLastBooking(null)
                );

        bookingRepository.findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(itemId, BookingStatus.APPROVED, now)
                .ifPresentOrElse(
                        nextBooking -> itemDto.setNextBooking(BookingDtoForItem.toDto(nextBooking)),
                        () -> itemDto.setNextBooking(null)
                );
    }

    @Override
    @Transactional
    public CommentDto addComment(CommentDto dto, Long userId, Long itemId) {
        log.info("SERVER: Добавление комментария к вещи {} пользователем {}: {}", itemId, userId, dto);

        itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.warn(MSG_ITEM_NOT_FOUND + itemId);
                    return new NotFoundException(MSG_ITEM_NOT_FOUND + itemId);
                });

        LocalDateTime now = LocalDateTime.now();

        boolean hasBooking = bookingRepository.existsByItemIdAndBookerIdAndStatusAndEndBefore(
                itemId, userId, BookingStatus.APPROVED, now
        );

        if (!hasBooking) {
            log.warn(MSG_COMMENT_FORBIDDEN);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, MSG_COMMENT_FORBIDDEN);
        }

        Comment saved = commentRepository.save(commentMapper.toComment(dto, itemId, userId));
        CommentDto result = commentMapper.toCommentDto(saved);

        log.info("SERVER: Комментарий добавлен: {}", result);
        return result;
    }
}
