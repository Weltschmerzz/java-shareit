package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingDto;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock BookingRepository bookingRepository;
    @Mock ItemRepository itemRepository;
    @Mock UserRepository userRepository;

    BookingServiceImpl bookingService;

    @BeforeEach
    void setUp() {
        bookingService = new BookingServiceImpl(bookingRepository, itemRepository, userRepository);
    }

    @Test
    void create_happyPath_shouldSaveWaiting() {
        long bookerId = 2L;
        User booker = User.builder().id(bookerId).name("B").email("b@mail.com").build();
        Item item = Item.builder().id(10L).name("Drill").description("d").available(true).ownerId(1L).build();

        when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));

        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setId(100L);
            return b;
        });

        BookingDto in = BookingDto.builder()
                .itemId(10L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        BookingDto out = bookingService.create(bookerId, in);

        assertThat(out.getId()).isEqualTo(100L);
        assertThat(out.getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(out.getItem().getId()).isEqualTo(10L);
        assertThat(out.getBooker().getId()).isEqualTo(2L);
    }

    @Test
    void create_whenOwnerBooksOwnItem_shouldThrow404() {
        long ownerId = 1L;
        User owner = User.builder().id(ownerId).name("O").email("o@mail.com").build();
        Item item = Item.builder().id(10L).name("Drill").description("d").available(true).ownerId(ownerId).build();

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));

        BookingDto in = BookingDto.builder()
                .itemId(10L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        assertThatThrownBy(() -> bookingService.create(ownerId, in))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void create_whenItemUnavailable_shouldThrow400() {
        long bookerId = 2L;
        User booker = User.builder().id(bookerId).name("B").email("b@mail.com").build();
        Item item = Item.builder().id(10L).name("Drill").description("d").available(false).ownerId(1L).build();

        when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));

        BookingDto in = BookingDto.builder()
                .itemId(10L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        assertThatThrownBy(() -> bookingService.create(bookerId, in))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void create_whenDatesInvalid_shouldThrow400() {
        long bookerId = 2L;
        User booker = User.builder().id(bookerId).name("B").email("b@mail.com").build();
        Item item = Item.builder().id(10L).name("Drill").description("d").available(true).ownerId(1L).build();

        when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));

        // end <= start
        BookingDto in1 = BookingDto.builder()
                .itemId(10L)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        assertThatThrownBy(() -> bookingService.create(bookerId, in1))
                .isInstanceOf(ValidationException.class);

        // start in past
        BookingDto in2 = BookingDto.builder()
                .itemId(10L)
                .start(LocalDateTime.now().minusMinutes(1))
                .end(LocalDateTime.now().plusDays(1))
                .build();

        assertThatThrownBy(() -> bookingService.create(bookerId, in2))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void approve_happyPath_shouldSetApprovedOrRejected() {
        long ownerId = 1L;
        Item item = Item.builder().id(10L).name("Drill").description("d").available(true).ownerId(ownerId).build();
        User booker = User.builder().id(2L).name("B").email("b@mail.com").build();
        Booking booking = Booking.builder()
                .id(100L)
                .item(item)
                .booker(booker)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.WAITING)
                .build();

        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        BookingDto approved = bookingService.approve(ownerId, 100L, true);
        assertThat(approved.getStatus()).isEqualTo(BookingStatus.APPROVED);

        // вернем WAITING снова, чтобы покрыть ветку REJECTED
        booking.setStatus(BookingStatus.WAITING);
        BookingDto rejected = bookingService.approve(ownerId, 100L, false);
        assertThat(rejected.getStatus()).isEqualTo(BookingStatus.REJECTED);
    }

    @Test
    void approve_whenNotOwner_shouldThrow403() {
        long ownerId = 1L;
        Item item = Item.builder().id(10L).name("Drill").description("d").available(true).ownerId(ownerId).build();
        Booking booking = Booking.builder().id(100L).item(item).status(BookingStatus.WAITING)
                .start(LocalDateTime.now().plusDays(1)).end(LocalDateTime.now().plusDays(2))
                .booker(User.builder().id(2L).name("B").email("b@mail.com").build())
                .build();

        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.approve(999L, 100L, true))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void approve_whenStatusNotWaiting_shouldThrow400() {
        long ownerId = 1L;
        Item item = Item.builder().id(10L).name("Drill").description("d").available(true).ownerId(ownerId).build();
        Booking booking = Booking.builder().id(100L).item(item).status(BookingStatus.APPROVED)
                .start(LocalDateTime.now().plusDays(1)).end(LocalDateTime.now().plusDays(2))
                .booker(User.builder().id(2L).name("B").email("b@mail.com").build())
                .build();

        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.approve(ownerId, 100L, true))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void getById_whenNeitherBookerNorOwner_shouldThrow404() {
        Item item = Item.builder().id(10L).name("Drill").description("d").available(true).ownerId(1L).build();
        User booker = User.builder().id(2L).name("B").email("b@mail.com").build();
        Booking booking = Booking.builder()
                .id(100L).item(item).booker(booker)
                .start(LocalDateTime.now().plusDays(1)).end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.WAITING)
                .build();

        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.getById(777L, 100L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getUserBookings_shouldRouteByState_andValidatePaging_andValidateState() {
        long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(User.builder().id(userId).name("U").email("u@mail.com").build()));

        Booking sample = Booking.builder()
                .id(1L)
                .item(Item.builder().id(10L).name("I").description("d").available(true).ownerId(2L).build())
                .booker(User.builder().id(userId).name("U").email("u@mail.com").build())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.WAITING)
                .build();

        when(bookingRepository.findAllByBooker_IdOrderByStartDesc(eq(userId), any(Pageable.class))).thenReturn(List.of(sample));
        when(bookingRepository.findAllByBooker_IdAndStartLessThanEqualAndEndGreaterThanEqualOrderByStartDesc(eq(userId), any(), any(), any(Pageable.class))).thenReturn(List.of(sample));
        when(bookingRepository.findAllByBooker_IdAndEndBeforeOrderByStartDesc(eq(userId), any(), any(Pageable.class))).thenReturn(List.of(sample));
        when(bookingRepository.findAllByBooker_IdAndStartAfterOrderByStartDesc(eq(userId), any(), any(Pageable.class))).thenReturn(List.of(sample));
        when(bookingRepository.findAllByBooker_IdAndStatusOrderByStartDesc(eq(userId), eq(BookingStatus.WAITING), any(Pageable.class))).thenReturn(List.of(sample));
        when(bookingRepository.findAllByBooker_IdAndStatusOrderByStartDesc(eq(userId), eq(BookingStatus.REJECTED), any(Pageable.class))).thenReturn(List.of(sample));

        assertThat(bookingService.getUserBookings(userId, "ALL", 0, 10)).hasSize(1);
        assertThat(bookingService.getUserBookings(userId, "CURRENT", 0, 10)).hasSize(1);
        assertThat(bookingService.getUserBookings(userId, "PAST", 0, 10)).hasSize(1);
        assertThat(bookingService.getUserBookings(userId, "FUTURE", 0, 10)).hasSize(1);
        assertThat(bookingService.getUserBookings(userId, "WAITING", 0, 10)).hasSize(1);
        assertThat(bookingService.getUserBookings(userId, "REJECTED", 0, 10)).hasSize(1);

        assertThatThrownBy(() -> bookingService.getUserBookings(userId, "UNKNOWN", 0, 10))
                .isInstanceOf(ValidationException.class);

        assertThatThrownBy(() -> bookingService.getUserBookings(userId, "ALL", -1, 10))
                .isInstanceOf(ValidationException.class);

        assertThatThrownBy(() -> bookingService.getUserBookings(userId, "ALL", 0, 0))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void getOwnerBookings_shouldRouteByState() {
        long ownerId = 2L;
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(User.builder().id(ownerId).name("O").email("o@mail.com").build()));

        Booking sample = Booking.builder()
                .id(1L)
                .item(Item.builder().id(10L).name("I").description("d").available(true).ownerId(ownerId).build())
                .booker(User.builder().id(1L).name("U").email("u@mail.com").build())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.WAITING)
                .build();

        when(bookingRepository.findAllByItem_OwnerIdOrderByStartDesc(eq(ownerId), any(Pageable.class))).thenReturn(List.of(sample));
        when(bookingRepository.findAllByItem_OwnerIdAndStartLessThanEqualAndEndGreaterThanEqualOrderByStartDesc(eq(ownerId), any(), any(), any(Pageable.class))).thenReturn(List.of(sample));
        when(bookingRepository.findAllByItem_OwnerIdAndEndBeforeOrderByStartDesc(eq(ownerId), any(), any(Pageable.class))).thenReturn(List.of(sample));
        when(bookingRepository.findAllByItem_OwnerIdAndStartAfterOrderByStartDesc(eq(ownerId), any(), any(Pageable.class))).thenReturn(List.of(sample));
        when(bookingRepository.findAllByItem_OwnerIdAndStatusOrderByStartDesc(eq(ownerId), eq(BookingStatus.WAITING), any(Pageable.class))).thenReturn(List.of(sample));
        when(bookingRepository.findAllByItem_OwnerIdAndStatusOrderByStartDesc(eq(ownerId), eq(BookingStatus.REJECTED), any(Pageable.class))).thenReturn(List.of(sample));

        assertThat(bookingService.getOwnerBookings(ownerId, "ALL", 0, 10)).hasSize(1);
        assertThat(bookingService.getOwnerBookings(ownerId, "CURRENT", 0, 10)).hasSize(1);
        assertThat(bookingService.getOwnerBookings(ownerId, "PAST", 0, 10)).hasSize(1);
        assertThat(bookingService.getOwnerBookings(ownerId, "FUTURE", 0, 10)).hasSize(1);
        assertThat(bookingService.getOwnerBookings(ownerId, "WAITING", 0, 10)).hasSize(1);
        assertThat(bookingService.getOwnerBookings(ownerId, "REJECTED", 0, 10)).hasSize(1);
    }
}
