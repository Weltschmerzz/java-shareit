package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.booking.service.BookingService;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class BookingServiceIT {

    @Autowired
    UserService userService;

    @Autowired
    ItemService itemService;

    @Autowired
    BookingService bookingService;

    @Test
    void createAndApprove_shouldPersistAndChangeStatus() {
        UserDto owner = userService.create(UserDto.builder()
                .name("Owner")
                .email("owner2@mail.com")
                .build());

        UserDto booker = userService.create(UserDto.builder()
                .name("Booker")
                .email("booker@mail.com")
                .build());

        ItemDto item = itemService.create(owner.getId(), ItemDto.builder()
                .name("Drill")
                .description("desc")
                .available(true)
                .build());

        BookingDto created = bookingService.create(booker.getId(), BookingDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build());

        BookingDto approved = bookingService.approve(owner.getId(), created.getId(), true);

        assertThat(approved.getId()).isEqualTo(created.getId());

        // проверим, что в выдаче бронирований пользователя эта бронь есть
        List<BookingDto> userBookings = bookingService.getUserBookings(booker.getId(), "ALL", 0, 10);
        assertThat(userBookings).extracting(BookingDto::getId).contains(created.getId());
    }
}
