package ru.practicum.shareit.request;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.Map;

@Service
public class RequestClient extends BaseClient {

    public RequestClient(RestTemplate rest, @Value("${shareit-server.url}") String serverUrl) {
        super(rest, serverUrl);
    }

    public ResponseEntity<Object> create(long userId, ItemRequestDto dto) {
        return post("/requests", userId, dto);
    }

    public ResponseEntity<Object> getUserRequests(long userId) {
        return get("/requests", userId);
    }

    public ResponseEntity<Object> getOtherUsersRequests(long userId, int from, int size) {
        return get("/requests/all?from={from}&size={size}", userId, Map.of(
                "from", from,
                "size", size
        ));
    }

    public ResponseEntity<Object> getById(long userId, long requestId) {
        return get("/requests/{requestId}", userId, Map.of("requestId", requestId));
    }
}
