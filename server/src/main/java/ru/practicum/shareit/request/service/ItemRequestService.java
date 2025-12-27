package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestService {

    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;

    private static final String MSG_USER_NOT_FOUND = "Пользователь не найден: ";
    private static final String MSG_REQUEST_NOT_FOUND = "Запрос не найден: ";

    @Transactional
    public ItemRequestDto create(ItemRequestDto dto, Long userId) {
        log.info("SERVER: Создание запроса {} для пользователя {}", dto, userId);
        User requestor = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(MSG_USER_NOT_FOUND + userId));
        ItemRequest request = ItemRequestMapper.toItemRequest(dto, requestor);
        ItemRequest saved = requestRepository.save(request);
        log.info("SERVER: Запрос создан {}", saved);
        return ItemRequestMapper.toItemRequestDto(saved);
    }

    public List<ItemRequestDto> getOwnRequests(Long userId) {
        log.info("SERVER: Получение собственных запросов пользователя {}", userId);
        User requestor = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(MSG_USER_NOT_FOUND + userId));
        List<ItemRequest> requests = requestRepository.findAllByRequestorOrderByCreatedDesc(requestor);
        List<ItemRequestDto> dtoList = requests.stream()
                .map(ItemRequestMapper::toItemRequestDto)
                .collect(Collectors.toList());
        log.info("SERVER: Найдено {} собственных запросов", dtoList.size());
        return dtoList;
    }

    public List<ItemRequestDto> getAllRequests(Long userId) {
        log.info("SERVER: Получение всех запросов, кроме пользователя {}", userId);
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(MSG_USER_NOT_FOUND + userId));
        List<ItemRequest> requests = requestRepository.findAllByRequestorIdNotOrderByCreatedDesc(userId);
        List<ItemRequestDto> dtoList = requests.stream()
                .map(ItemRequestMapper::toItemRequestDto)
                .collect(Collectors.toList());
        log.info("SERVER: Найдено {} запросов от других пользователей", dtoList.size());
        return dtoList;
    }

    public ItemRequestDto getRequestById(Long requestId, Long userId) {
        log.info("SERVER: Получение запроса {} для пользователя {}", requestId, userId);
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(MSG_USER_NOT_FOUND + userId));
        ItemRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(MSG_REQUEST_NOT_FOUND + requestId));
        ItemRequestDto dto = ItemRequestMapper.toDto(request, true);
        log.info("SERVER: Найден запрос {}", dto);
        return dto;
    }
}
