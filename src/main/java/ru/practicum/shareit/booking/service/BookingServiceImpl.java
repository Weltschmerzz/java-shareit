package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.*;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BookingDto create(Long userId, BookingDto dto) {

        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + userId));

        Item item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь не найдена: " + dto.getItemId()));

        if (item.getOwnerId().equals(userId)) {
            throw new NotFoundException("Владелец не можем забронировать собственную вещь");
        }

        if (!Boolean.TRUE.equals(item.getAvailable())) {
            throw new ValidationException("Вещь не доступна для бронирования");
        }

        validateDates(dto.getStart(), dto.getEnd());

        Booking booking = Booking.builder()
                .start(dto.getStart())
                .end(dto.getEnd())
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();

        return BookingMapper.toDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingDto approve(Long ownerId, Long bookingId, boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено: " + bookingId));

        if (!booking.getItem().getOwnerId().equals(ownerId)) {
            throw new ForbiddenException("Только владелец может подтверждать или отклонять бронирование");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Статус бронирования отличный от WAITING");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return BookingMapper.toDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto getById(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено: " + bookingId));

        boolean isBooker = booking.getBooker().getId().equals(userId);
        boolean isOwner = booking.getItem().getOwnerId().equals(userId);

        if (!isBooker && !isOwner) {
            throw new NotFoundException("Бронирование не найдено: " + bookingId);
        }

        return BookingMapper.toDto(booking);
    }

    @Override
    public List<BookingDto> getUserBookings(Long userId, String state, int from, int size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + userId));

        validatePaging(from, size);
        BookingState st = BookingState.from(state);
        LocalDateTime now = LocalDateTime.now();

        Pageable pageable = PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "start"));

        List<Booking> bookings = switch (st) {
            case ALL -> bookingRepository.findAllByBooker_IdOrderByStartDesc(userId, pageable);
            case CURRENT -> bookingRepository.findAllByBooker_IdAndStartLessThanEqualAndEndGreaterThanEqualOrderByStartDesc(userId, now, now, pageable);
            case PAST -> bookingRepository.findAllByBooker_IdAndEndBeforeOrderByStartDesc(userId, now, pageable);
            case FUTURE -> bookingRepository.findAllByBooker_IdAndStartAfterOrderByStartDesc(userId, now, pageable);
            case WAITING -> bookingRepository.findAllByBooker_IdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING, pageable);
            case REJECTED -> bookingRepository.findAllByBooker_IdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED, pageable);
        };

        return bookings.stream().map(BookingMapper::toDto).toList();
    }

    @Override
    public List<BookingDto> getOwnerBookings(Long ownerId, String state, int from, int size) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + ownerId));

        validatePaging(from, size);
        BookingState st = BookingState.from(state);
        LocalDateTime now = LocalDateTime.now();

        Pageable pageable = PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "start"));

        List<Booking> bookings = switch (st) {
            case ALL -> bookingRepository.findAllByItem_OwnerIdOrderByStartDesc(ownerId, pageable);
            case CURRENT -> bookingRepository.findAllByItem_OwnerIdAndStartLessThanEqualAndEndGreaterThanEqualOrderByStartDesc(ownerId, now, now, pageable);
            case PAST -> bookingRepository.findAllByItem_OwnerIdAndEndBeforeOrderByStartDesc(ownerId, now, pageable);
            case FUTURE -> bookingRepository.findAllByItem_OwnerIdAndStartAfterOrderByStartDesc(ownerId, now, pageable);
            case WAITING -> bookingRepository.findAllByItem_OwnerIdAndStatusOrderByStartDesc(ownerId, BookingStatus.WAITING, pageable);
            case REJECTED -> bookingRepository.findAllByItem_OwnerIdAndStatusOrderByStartDesc(ownerId, BookingStatus.REJECTED, pageable);
        };

        return bookings.stream().map(BookingMapper::toDto).toList();
    }

    private void validateDates(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new ValidationException("Временный метки должны быть указаны");
        }
        if (!end.isAfter(start)) {
            throw new ValidationException("Дата окончания аренды должна быть после даты начала");
        }
        if (start.isBefore(LocalDateTime.now())) {
            throw new ValidationException("Дата начала должна быть в будущем");
        }
    }

    private void validatePaging(int from, int size) {
        if (from < 0) throw new ValidationException("from должен быть >= 0");
        if (size <= 0) throw new ValidationException("size должен быть > 0");
    }
}