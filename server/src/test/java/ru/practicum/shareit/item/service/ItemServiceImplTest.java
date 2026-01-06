package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock UserService userService;
    @Mock ItemRepository itemRepository;
    @Mock BookingRepository bookingRepository;
    @Mock CommentRepository commentRepository;
    @Mock UserRepository userRepository;

    ItemServiceImpl itemService;

    @BeforeEach
    void setUp() {
        itemService = new ItemServiceImpl(userService, itemRepository, bookingRepository, commentRepository, userRepository);
    }

    @Test
    void search_whenBlank_shouldReturnEmptyAndNotHitRepo() {
        assertThat(itemService.search("   ")).isEmpty();
        verify(itemRepository, never()).search(anyString());
    }

    @Test
    void update_whenNotOwner_shouldThrow404() {
        long ownerId = 1L;
        when(userService.getById(ownerId)).thenReturn(UserDto.builder().id(ownerId).name("O").email("o@mail.com").build());

        Item existing = Item.builder().id(10L).name("I").description("d").available(true).ownerId(999L).build();
        when(itemRepository.findById(10L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> itemService.update(ownerId, 10L, ItemDto.builder().name("X").build()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getById_whenOwner_shouldIncludeLastNextAndComments() {
        long ownerId = 1L;
        long itemId = 10L;

        Item item = Item.builder().id(itemId).name("I").description("d").available(true).ownerId(ownerId).build();
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        User booker = User.builder().id(2L).name("B").email("b@mail.com").build();

        Booking last = Booking.builder().id(100L).item(item).booker(booker)
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().minusDays(1))
                .status(BookingStatus.APPROVED)
                .build();

        Booking next = Booking.builder().id(200L).item(item).booker(booker)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.APPROVED)
                .build();

        when(bookingRepository.findFirstByItem_IdAndStatusAndStartLessThanEqualOrderByStartDesc(eq(itemId), eq(BookingStatus.APPROVED), any()))
                .thenReturn(Optional.of(last));

        when(bookingRepository.findFirstByItem_IdAndStatusAndStartAfterOrderByStartAsc(eq(itemId), eq(BookingStatus.APPROVED), any()))
                .thenReturn(Optional.of(next));

        Comment c = Comment.builder().id(1L).text("Nice").item(item).author(User.builder().id(3L).name("A").email("a@mail.com").build())
                .created(LocalDateTime.now()).build();
        when(commentRepository.findAllByItem_IdOrderByCreatedDesc(itemId)).thenReturn(List.of(c));

        ItemResponseDto out = itemService.getById(ownerId, itemId);

        assertThat(out.getLastBooking()).isNotNull();
        assertThat(out.getLastBooking().getId()).isEqualTo(100L);

        assertThat(out.getNextBooking()).isNotNull();
        assertThat(out.getNextBooking().getId()).isEqualTo(200L);

        assertThat(out.getComments()).hasSize(1);
        assertThat(out.getComments().get(0).getAuthorName()).isEqualTo("A");
    }

    @Test
    void getById_whenNotOwner_shouldNotIncludeBookings() {
        long ownerId = 1L;
        long otherUserId = 999L;
        long itemId = 10L;

        Item item = Item.builder().id(itemId).name("I").description("d").available(true).ownerId(ownerId).build();
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(commentRepository.findAllByItem_IdOrderByCreatedDesc(itemId)).thenReturn(List.of());

        ItemResponseDto out = itemService.getById(otherUserId, itemId);

        assertThat(out.getLastBooking()).isNull();
        assertThat(out.getNextBooking()).isNull();
        verify(bookingRepository, never()).findFirstByItem_IdAndStatusAndStartLessThanEqualOrderByStartDesc(any(), any(), any());
        verify(bookingRepository, never()).findFirstByItem_IdAndStatusAndStartAfterOrderByStartAsc(any(), any(), any());
    }

    @Test
    void addComment_whenNoCompletedBooking_shouldThrow400() {
        long userId = 1L;
        long itemId = 10L;

        when(userRepository.findById(userId)).thenReturn(Optional.of(User.builder().id(userId).name("U").email("u@mail.com").build()));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(Item.builder().id(itemId).name("I").description("d").available(true).ownerId(2L).build()));
        when(bookingRepository.existsByItem_IdAndBooker_IdAndStatusAndEndBefore(eq(itemId), eq(userId), eq(BookingStatus.APPROVED), any()))
                .thenReturn(false);

        assertThatThrownBy(() -> itemService.addComment(userId, itemId, CommentCreateDto.builder().text("x").build()))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void addComment_success_shouldSaveAndReturnDto() {
        long userId = 1L;
        long itemId = 10L;

        User author = User.builder().id(userId).name("U").email("u@mail.com").build();
        Item item = Item.builder().id(itemId).name("I").description("d").available(true).ownerId(2L).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(author));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.existsByItem_IdAndBooker_IdAndStatusAndEndBefore(eq(itemId), eq(userId), eq(BookingStatus.APPROVED), any()))
                .thenReturn(true);

        when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> {
            Comment c = inv.getArgument(0);
            c.setId(99L);
            return c;
        });

        CommentDto out = itemService.addComment(userId, itemId, CommentCreateDto.builder().text("Nice").build());

        assertThat(out.getId()).isEqualTo(99L);
        assertThat(out.getText()).isEqualTo("Nice");
        assertThat(out.getAuthorName()).isEqualTo("U");
    }

    @Test
    void delete_shouldDeleteItem() {
        long itemId = 10L;
        Item item = Item.builder().id(itemId).name("I").description("d").available(true).ownerId(1L).build();

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        itemService.delete(itemId);

        verify(itemRepository).delete(item);
    }

    @Test
    void getOwnerItems_shouldBuildResponses() {
        long ownerId = 1L;
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(User.builder().id(ownerId).name("O").email("o@mail.com").build()));

        Item i1 = Item.builder().id(10L).name("I1").description("d").available(true).ownerId(ownerId).build();
        when(itemRepository.findAllByOwnerIdOrderByIdAsc(ownerId)).thenReturn(List.of(i1));

        when(bookingRepository.findFirstByItem_IdAndStatusAndStartLessThanEqualOrderByStartDesc(eq(10L), eq(BookingStatus.APPROVED), any()))
                .thenReturn(Optional.empty());
        when(bookingRepository.findFirstByItem_IdAndStatusAndStartAfterOrderByStartAsc(eq(10L), eq(BookingStatus.APPROVED), any()))
                .thenReturn(Optional.empty());
        when(commentRepository.findAllByItem_IdOrderByCreatedDesc(10L)).thenReturn(List.of());

        var out = itemService.getOwnerItems(ownerId);
        assertThat(out).hasSize(1);
        assertThat(out.get(0).getName()).isEqualTo("I1");
    }
}
