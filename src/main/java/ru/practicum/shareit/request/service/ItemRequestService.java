package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {

    ItemRequestDto create(Long userId, ItemRequestDto dto);

    List<ItemRequestDto> getUserRequests(Long userId);

    List<ItemRequestDto> getOtherUsersRequests(Long userId, int from, int size);

    ItemRequestDto getById(Long userId, Long requestId);
}
