package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final UserService userService;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;


    @Override
    @Transactional
    public ItemDto create(Long ownerId, ItemDto dto) {

        userService.getById(ownerId);

        Item item = ItemMapper.toItem(dto, ownerId);
        Item saved = itemRepository.save(item);

        return ItemMapper.toItemDto(saved);
    }

    @Override
    @Transactional
    public ItemDto update(Long ownerId, Long itemId, ItemDto dto) {
        userService.getById(ownerId);

        Item existing = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена: " + itemId));

        if (!Objects.equals(existing.getOwnerId(), ownerId)) {
            throw new NotFoundException("Пользователь не найден: " + ownerId);
        }

        if (dto.getName() != null) existing.setName(dto.getName());
        if (dto.getDescription() != null) existing.setDescription(dto.getDescription());
        if (dto.getAvailable() != null) existing.setAvailable(dto.getAvailable());
        if (dto.getRequestId() != null) existing.setRequestId(dto.getRequestId());

        Item saved = itemRepository.save(existing);
        return ItemMapper.toItemDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ItemResponseDto getById(Long userId, Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена: " + itemId));

        List<CommentDto> comments = commentRepository.findAllByItem_IdOrderByCreatedDesc(itemId)
                .stream()
                .map(this::toCommentDto)
                .toList();

        boolean isOwner = item.getOwnerId().equals(userId);

        BookingShortDto last = null;
        BookingShortDto next = null;

        if (isOwner) {
            LocalDateTime now = LocalDateTime.now();
            last = bookingRepository
                    .findFirstByItem_IdAndStatusAndStartLessThanEqualOrderByStartDesc(itemId, BookingStatus.APPROVED, now)
                    .map(this::toBookingShortDto)
                    .orElse(null);

            next = bookingRepository
                    .findFirstByItem_IdAndStatusAndStartAfterOrderByStartAsc(itemId, BookingStatus.APPROVED, now)
                    .map(this::toBookingShortDto)
                    .orElse(null);
        }

        return ItemResponseDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .requestId(item.getRequestId())
                .lastBooking(last)
                .nextBooking(next)
                .comments(comments)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemResponseDto> getOwnerItems(Long userId) {

        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + userId));

        List<Item> items = itemRepository.findAllByOwnerIdOrderByIdAsc(userId);

        LocalDateTime now = LocalDateTime.now();

        return items.stream().map(item -> {
            Long itemId = item.getId();

            BookingShortDto last = bookingRepository
                    .findFirstByItem_IdAndStatusAndStartLessThanEqualOrderByStartDesc(itemId, BookingStatus.APPROVED, now)
                    .map(this::toBookingShortDto)
                    .orElse(null);

            BookingShortDto next = bookingRepository
                    .findFirstByItem_IdAndStatusAndStartAfterOrderByStartAsc(itemId, BookingStatus.APPROVED, now)
                    .map(this::toBookingShortDto)
                    .orElse(null);

            List<CommentDto> comments = commentRepository.findAllByItem_IdOrderByCreatedDesc(itemId)
                    .stream()
                    .map(this::toCommentDto)
                    .toList();

            return ItemResponseDto.builder()
                    .id(item.getId())
                    .name(item.getName())
                    .description(item.getDescription())
                    .available(item.getAvailable())
                    .requestId(item.getRequestId())
                    .lastBooking(last)
                    .nextBooking(next)
                    .comments(comments)
                    .build();
        }).toList();
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return itemRepository.search(text).stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    @Transactional
    public void delete(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена: " + itemId));
        itemRepository.delete(item);
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, CommentCreateDto dto) {

        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + userId));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена: " + itemId));

        //была APPROVED аренда и уже закончилась
        boolean canComment = bookingRepository.existsByItem_IdAndBooker_IdAndStatusAndEndBefore(
                itemId, userId, BookingStatus.APPROVED, LocalDateTime.now());

        if (!canComment) {
            throw new ValidationException("Пользователь не завершил бронирование данной вещи");
        }

        Comment comment = Comment.builder()
                .text(dto.getText())
                .item(item)
                .author(author)
                .created(LocalDateTime.now().withNano(0))
                .build();

        Comment saved = commentRepository.save(comment);

        return CommentDto.builder()
                .id(saved.getId())
                .text(saved.getText())
                .authorName(author.getName())
                .created(saved.getCreated())
                .build();
    }

    private BookingShortDto toBookingShortDto(Booking booking) {
        return BookingShortDto.builder()
                .id(booking.getId())
                .bookerId(booking.getBooker().getId())
                .build();
    }

    private CommentDto toCommentDto(Comment c) {
        return CommentDto.builder()
                .id(c.getId())
                .text(c.getText())
                .authorName(c.getAuthor().getName())
                .created(c.getCreated())
                .build();
    }
}
