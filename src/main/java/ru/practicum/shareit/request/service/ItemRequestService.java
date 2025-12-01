package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestService {
    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;

    @Transactional
    public ItemRequestDto create(ItemRequestDto dto, Long userId) {
        User requestor = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
        ItemRequest request = ItemRequestDto.toEntity(dto, requestor);
        request = requestRepository.save(request);
        return ItemRequestDto.toDto(request);
    }

    public List<ItemRequestDto> getOwnRequests(Long userId) {
        User requestor = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
        List<ItemRequest> requests = requestRepository.findAllByRequestorOrderByCreatedDesc(requestor);
        return requests.stream().map(ItemRequestDto::toDto).collect(Collectors.toList());
    }

    public List<ItemRequestDto> getAllRequests(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
        List<ItemRequest> requests = requestRepository.findAllByRequestorIdNotOrderByCreatedDesc(userId);
        return requests.stream().map(ItemRequestDto::toDto).collect(Collectors.toList());
    }

    public ItemRequestDto getRequestById(Long requestId, Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
        ItemRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request not found: " + requestId));
        return ItemRequestDto.toDto(request);
    }
}