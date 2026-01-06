package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.client.BaseClient;

import java.util.Map;

@Service
public class BookingClient extends BaseClient {

    public BookingClient(RestTemplate rest,
                         @Value("${shareit-server.url}") String serverUrl) {
        super(rest, serverUrl);
    }

    public ResponseEntity<Object> create(long userId, BookingDto dto) {
        return post("/bookings", userId, dto);
    }

    public ResponseEntity<Object> approve(long userId, long bookingId, boolean approved) {
        return patch("/bookings/{bookingId}?approved={approved}", userId,
                Map.of("bookingId", bookingId, "approved", approved));
    }

    public ResponseEntity<Object> getById(long userId, long bookingId) {
        return get("/bookings/{bookingId}", userId, Map.of("bookingId", bookingId));
    }

    public ResponseEntity<Object> getByBooker(long userId, String state, int from, int size) {
        return get("/bookings?state={state}&from={from}&size={size}", userId,
                Map.of("state", state, "from", from, "size", size));
    }

    public ResponseEntity<Object> getByOwner(long userId, String state, int from, int size) {
        return get("/bookings/owner?state={state}&from={from}&size={size}", userId,
                Map.of("state", state, "from", from, "size", size));
    }
}
