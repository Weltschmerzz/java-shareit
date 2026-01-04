package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDto {

    private Long id;

    @NotNull(message = "itemId обязателен")
    private Long itemId;

    @NotNull(message = "start обязателен")
    private LocalDateTime start;

    @NotNull(message = "end обязателен")
    private LocalDateTime end;

    private BookingStatus status;
    private ItemShortDto item;
    private UserShortDto booker;
}