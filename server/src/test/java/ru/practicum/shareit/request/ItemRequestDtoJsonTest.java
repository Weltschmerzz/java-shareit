package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestDtoJsonTest {

    @Autowired
    JacksonTester<ItemRequestDto> json;

    @Test
    void itemRequestDto_shouldSerializeCreatedWithSeconds() throws Exception {
        ItemRequestDto dto = ItemRequestDto.builder()
                .id(1L)
                .description("need drill")
                .created(LocalDateTime.of(2026, 1, 5, 14, 14, 9))
                .build();

        var content = json.write(dto);

        assertThat(content).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(content).extractingJsonPathStringValue("$.description").isEqualTo("need drill");
        assertThat(content).extractingJsonPathStringValue("$.created").isEqualTo("2026-01-05T14:14:09");
    }
}
