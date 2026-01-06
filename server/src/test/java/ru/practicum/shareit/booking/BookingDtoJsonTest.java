package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingDtoJsonTest {

    @Autowired
    JacksonTester<BookingDto> json;

    @Test
    void bookingDto_shouldSerializeDates() throws Exception {
        BookingDto dto = BookingDto.builder()
                .itemId(10L)
                .start(LocalDateTime.of(2026, 1, 6, 17, 34, 58))
                .end(LocalDateTime.of(2026, 1, 7, 17, 34, 58))
                .build();

        var content = json.write(dto);

        assertThat(content).extractingJsonPathNumberValue("$.itemId").isEqualTo(10);
        assertThat(content).extractingJsonPathStringValue("$.start").isEqualTo("2026-01-06T17:34:58");
        assertThat(content).extractingJsonPathStringValue("$.end").isEqualTo("2026-01-07T17:34:58");
    }
}
