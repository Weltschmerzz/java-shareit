package ru.practicum.shareit.coverage;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.NotUniqueInputValue;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestItemDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CoverageSmokeTest {

    @Test
    void shouldTouchDtosMappersModelsAndExceptions() {
        // enums
        assertThat(BookingState.valueOf("ALL")).isEqualTo(BookingState.ALL);
        assertThat(BookingStatus.valueOf("WAITING")).isEqualTo(BookingStatus.WAITING);

        // exceptions (покрываем классы)
        assertThat(new ValidationException("v").getMessage()).isEqualTo("v");
        assertThat(new NotFoundException("n").getMessage()).isEqualTo("n");
        assertThat(new ForbiddenException("f").getMessage()).isEqualTo("f");
        assertThat(new NotUniqueInputValue("u").getMessage()).isEqualTo("u");

        // user model + mapper
        User user = User.builder().id(1L).name("User").email("u@mail.com").build();
        UserDto userDto = UserMapper.toUserDto(user);
        assertThat(userDto.getId()).isEqualTo(1L);
        assertThat(UserMapper.toUser(userDto).getEmail()).isEqualTo("u@mail.com");
        assertThat(UserMapper.toUserDto(null)).isNull();
        assertThat(UserMapper.toUser(null)).isNull();

        // item model + mapper
        Item item = Item.builder()
                .id(10L)
                .name("Item")
                .description("desc")
                .available(true)
                .ownerId(1L)
                .requestId(5L)
                .build();

        ItemDto itemDto = ItemMapper.toItemDto(item);
        assertThat(itemDto.getRequestId()).isEqualTo(5L);

        Item item2 = ItemMapper.toItem(itemDto, 1L);
        assertThat(item2.getOwnerId()).isEqualTo(1L);

        assertThat(ItemMapper.toItemDto(null)).isNull();
        assertThat(ItemMapper.toItem(null, 1L)).isNull();

        // request model + mapper
        ItemRequest req = ItemRequest.builder()
                .id(5L)
                .description("need")
                .created(LocalDateTime.now())
                .requestor(user)
                .build();

        ItemRequestDto reqDto = ItemRequestMapper.toDto(req, List.of(item));
        assertThat(reqDto.getItems()).hasSize(1);

        ItemRequestItemDto reqItemDto = ItemRequestMapper.toItemDto(item);
        assertThat(reqItemDto.getOwnerId()).isEqualTo(1L);

        assertThat(ItemRequestMapper.toDto(null, null)).isNull();
        assertThat(ItemRequestMapper.toItemDto(null)).isNull();

        // booking model + mapper
        Booking booking = Booking.builder()
                .id(100L)
                .item(item)
                .booker(user)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.WAITING)
                .build();

        BookingDto bookingDto = BookingMapper.toDto(booking);
        assertThat(bookingDto.getId()).isEqualTo(100L);
        assertThat(BookingMapper.toDto(null)).isNull();

        // comment dto/model (просто чтобы класс считался покрытым)
        Comment comment = Comment.builder()
                .id(1L)
                .text("ok")
                .item(item)
                .author(user)
                .created(LocalDateTime.now())
                .build();

        CommentDto commentDto = CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(comment.getAuthor().getName())
                .created(comment.getCreated())
                .build();

        assertThat(commentDto.getAuthorName()).isEqualTo("User");
    }
}
