package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;

import java.util.List;

public interface ItemService {

    ItemDto create(Long ownerId, ItemDto dto);

    ItemDto update(Long ownerId, Long itemId, ItemDto dto);

    ItemResponseDto getById(Long userId, Long itemId);

    List<ItemResponseDto> getOwnerItems(Long userId);

    List<ItemDto> search(String text);

    void delete(Long id);

    CommentDto addComment(Long userId, Long itemId, CommentCreateDto dto);
}
