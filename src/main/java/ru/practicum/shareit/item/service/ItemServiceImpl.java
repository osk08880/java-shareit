package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.dto.BookingDtoForItem;
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
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final InMemoryItemRequestStorage requestStorage;
    private final InMemoryItemHistoryStorage historyStorage;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public ItemDto create(Long userId, ItemDto itemDto, Long requestId) {
        log.debug("Создание вещи: userId={}, itemDto={}, requestId={}", userId, itemDto, requestId);

        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        ItemRequest requestObj = null;
        if (requestId != null) {
            requestObj = requestStorage.findById(requestId)
                    .orElseThrow(() -> new NotFoundException("Запрос не найден"));
            if (requestObj.getRequestor().getId().equals(userId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Нельзя отвечать на свой запрос");
            }
            log.debug("Вещь будет создана в ответ на запрос {} пользователем {}", requestId, userId);
        }

        Item item = ItemMapper.toItem(itemDto, owner, requestObj);

        Item savedItem = itemRepository.save(item);
        log.info("Вещь создана пользователем {}: {}", userId, savedItem);

        ItemDto dto = ItemMapper.toItemDto(savedItem);
        enrichWithBookings(dto, savedItem.getId());

        return dto;
    }

    @Override
    @Transactional
    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        log.debug("Обновление вещи: userId={}, itemId={}, itemDto={}", userId, itemId, itemDto);

        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        if (!existingItem.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Только владелец может обновить вещь");
        }

        Item updatedItem = ItemMapper.toItem(itemDto, existingItem.getOwner(), existingItem.getRequest());

        Item savedItem = itemRepository.save(updatedItem);
        log.info("Вещь {} обновлена пользователем {}", itemId, userId);

        ItemDto dto = ItemMapper.toItemDto(savedItem);
        enrichWithBookings(dto, itemId);

        return dto;
    }

    @Override
    public ItemDto findById(Long userId, Long itemId) {
        log.debug("Получение вещи по ID: userId={}, itemId={}", userId, itemId);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Предмет с ID " + itemId + " не найден"));

        ItemDto dto = ItemMapper.toItemDto(item);

        if (item.getOwner().getId().equals(userId)) {
            enrichWithBookings(dto, itemId);
        } else {
            dto.setLastBooking(null);
            dto.setNextBooking(null);
        }

        historyStorage.addView(userId, itemId);

        log.info("Вещь {} просмотрена пользователем {}", itemId, userId);

        return dto;
    }

    @Override
    public List<ItemDto> findAllByOwner(Long userId) {
        log.debug("Получение всех вещей владельца: userId={}", userId);

        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        List<ItemDto> items = itemRepository.findByOwnerId(userId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());

        items.forEach(dto -> enrichWithBookings(dto, dto.getId()));

        log.info("Возвращено {} вещей для владельца {}", items.size(), userId);

        return items;
    }

    @Override
    public List<ItemDto> search(String text) {
        log.debug("Поиск вещей по тексту: text={}", text);

        if (text == null || text.isBlank()) return Collections.emptyList();

        List<ItemDto> results = itemRepository.searchAvailableItems(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());

        log.info("Найдено {} вещей по запросу '{}'", results.size(), text);

        return results;
    }

    private void enrichWithBookings(ItemDto itemDto, Long itemId) {
        LocalDateTime now = LocalDateTime.now();

        bookingRepository.findFirstByItemIdAndStatusAndEndBeforeOrderByEndDesc(itemId, BookingStatus.APPROVED, now)
                .ifPresentOrElse(
                        lastBooking -> {
                            BookingDtoForItem lastDto = BookingDtoForItem.toDto(lastBooking);
                            itemDto.setLastBooking(lastDto);
                        },
                        () -> itemDto.setLastBooking(null)
                );

        bookingRepository.findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(itemId, BookingStatus.APPROVED, now)
                .ifPresentOrElse(
                        nextBooking -> {
                            BookingDtoForItem nextDto = BookingDtoForItem.toDto(nextBooking);
                            itemDto.setNextBooking(nextDto);
                        },
                        () -> itemDto.setNextBooking(null)
                );
    }

    @Override
    @Transactional
    public CommentDto addComment(CommentDto dto, Long userId, Long itemId) {
        log.debug("Добавление комментария: dto={}, userId={}, itemId={}", dto, userId, itemId);

        itemRepository.findById(itemId).orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        LocalDateTime now = LocalDateTime.now();

        boolean hasBooking = bookingRepository.existsByItemIdAndBookerIdAndStatusAndEndBefore(itemId, userId, BookingStatus.APPROVED, now);

        if (!hasBooking) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Только тот, кто арендовал, может оставить отзыв");
        }

        Comment comment = commentMapper.toComment(dto, itemId, userId);
        Comment saved = commentRepository.save(comment);

        log.info("Комментарий добавлен к вещи {} пользователем {}", itemId, userId);

        return commentMapper.toCommentDto(saved);
    }
}