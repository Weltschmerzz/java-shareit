package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Map;

@Service
public class UserClient extends BaseClient {

    public UserClient(RestTemplate rest,
                      @Value("${shareit-server.url}") String serverUrl) {
        super(rest, serverUrl);
    }

    public ResponseEntity<Object> create(UserDto dto) {
        return post("/users", 0L, dto);
    }

    public ResponseEntity<Object> update(long userId, UserDto dto) {
        return patch("/users/{userId}", 0L, dto, Map.of("userId", userId));
    }

    public ResponseEntity<Object> getById(long userId) {
        return get("/users/{userId}", 0L, Map.of("userId", userId));
    }

    public ResponseEntity<Object> getAll() {
        return get("/users", 0L);
    }

    public ResponseEntity<Object> delete(long userId) {
        return delete("/users/{userId}", 0L, Map.of("userId", userId));
    }
}
