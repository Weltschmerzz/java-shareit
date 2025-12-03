package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.service.UserService;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final UserService userService;

    private final Map<Long, Item> items = new HashMap<>();
    private long seq = 0;

    @Override
    public ItemDto create(Long ownerId, ItemDto dto) {

        userService.getById(ownerId);

        Item item = ItemMapper.toItem(dto, ownerId);
        item.setId(seq++);
        items.put(item.getId(), item);

        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto update(Long ownerId, Long itemId, ItemDto dto) {

        userService.getById(ownerId);

        Item existing = items.get(itemId);

        if (existing == null) throw new NotFoundException("Item not found: " + itemId);
        if (!Objects.equals(existing.getOwnerId(), ownerId)) throw new NotFoundException("Owner not found: " + ownerId);

        if (dto.getName() != null) existing.setName(dto.getName());
        if (dto.getDescription() != null) existing.setDescription(dto.getDescription());
        if (dto.getAvailable() != null) existing.setAvailable(dto.getAvailable());
        if (dto.getRequestId() != null) existing.setRequestId(dto.getRequestId());

        return ItemMapper.toItemDto(existing);
    }

    @Override
    public ItemDto getById(Long userId, Long itemId) {

        userService.getById(userId);

        Item item = items.get(itemId);
        if (item == null) throw new NotFoundException("Item not found: " + itemId);

        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> getOwnerItems(Long ownerId) {

        userService.getById(ownerId);

        return items.values().stream()
                .filter(item -> Objects.equals(item.getOwnerId(), ownerId))
                .sorted(Comparator.comparing(Item::getId))
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        String query = text.toLowerCase();

        List<Item> matchedItems = new ArrayList<>();

        for (Item item : items.values()) {

            if (!Boolean.TRUE.equals(item.getAvailable())) {
                continue;
            }

            String name = item.getName();
            String description = item.getDescription();

            boolean nameMatches = name != null && name.toLowerCase().contains(query);
            boolean descriptionMatches = description != null && description.toLowerCase().contains(query);

            if (nameMatches || descriptionMatches) {
                matchedItems.add(item);
            }
        }

        matchedItems.sort(Comparator.comparing(Item::getId));

        List<ItemDto> result = new ArrayList<>();
        for (Item item : matchedItems) {
            result.add(ItemMapper.toItemDto(item));
        }

        return result;
    }

    @Override
    public void delete(Long itemId) {
        Item item = items.get(itemId);
        if (item == null) throw new NotFoundException("Item not found: " + itemId);
        items.remove(itemId);
    }
}
