package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @EntityGraph(attributePaths = {"item", "booker"})
    Optional<Booking> findById(Long id);

    @EntityGraph(attributePaths = {"item", "booker"})
    List<Booking> findAllByBooker_IdOrderByStartDesc(Long bookerId, Pageable pageable);

    @EntityGraph(attributePaths = {"item", "booker"})
    List<Booking> findAllByBooker_IdAndStartLessThanEqualAndEndGreaterThanEqualOrderByStartDesc(
            Long bookerId, LocalDateTime now1, LocalDateTime now2, Pageable pageable);

    @EntityGraph(attributePaths = {"item", "booker"})
    List<Booking> findAllByBooker_IdAndEndBeforeOrderByStartDesc(
            Long bookerId, LocalDateTime now, Pageable pageable);

    @EntityGraph(attributePaths = {"item", "booker"})
    List<Booking> findAllByBooker_IdAndStartAfterOrderByStartDesc(
            Long bookerId, LocalDateTime now, Pageable pageable);

    @EntityGraph(attributePaths = {"item", "booker"})
    List<Booking> findAllByBooker_IdAndStatusOrderByStartDesc(
            Long bookerId, BookingStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"item", "booker"})
    List<Booking> findAllByItem_OwnerIdOrderByStartDesc(Long ownerId, Pageable pageable);

    @EntityGraph(attributePaths = {"item", "booker"})
    List<Booking> findAllByItem_OwnerIdAndStartLessThanEqualAndEndGreaterThanEqualOrderByStartDesc(
            Long ownerId, LocalDateTime now1, LocalDateTime now2, Pageable pageable);

    @EntityGraph(attributePaths = {"item", "booker"})
    List<Booking> findAllByItem_OwnerIdAndEndBeforeOrderByStartDesc(
            Long ownerId, LocalDateTime now, Pageable pageable);

    @EntityGraph(attributePaths = {"item", "booker"})
    List<Booking> findAllByItem_OwnerIdAndStartAfterOrderByStartDesc(
            Long ownerId, LocalDateTime now, Pageable pageable);

    @EntityGraph(attributePaths = {"item", "booker"})
    List<Booking> findAllByItem_OwnerIdAndStatusOrderByStartDesc(
            Long ownerId, BookingStatus status, Pageable pageable);

    // last: start <= now, самый свежий в прошлом
    Optional<Booking> findFirstByItem_IdAndStatusAndStartLessThanEqualOrderByStartDesc(
            Long itemId, BookingStatus status, LocalDateTime now);

    // next: start > now, самый ближайший в будущем
    Optional<Booking> findFirstByItem_IdAndStatusAndStartAfterOrderByStartAsc(
            Long itemId, BookingStatus status, LocalDateTime now);

    // для comments: была APPROVED аренда и уже закончилась
    boolean existsByItem_IdAndBooker_IdAndStatusAndEndBefore(
            Long itemId, Long bookerId, BookingStatus status, LocalDateTime now);

}