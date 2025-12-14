package ru.practicum.shareit.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class NotUniqueInputValue extends RuntimeException {
    public NotUniqueInputValue(String message) {
        super(message);
    }
}
