package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Map;

@Service
public class ItemClient extends BaseClient {

    public ItemClient(RestTemplate rest,
                      @Value("${shareit-server.url}") String serverUrl) {
        super(rest, serverUrl);
    }

    public ResponseEntity<Object> create(long userId, ItemDto dto) {
        return post("/items", userId, dto);
    }

    public ResponseEntity<Object> update(long userId, long itemId, ItemDto dto) {
        return patch("/items/{itemId}", userId, dto, Map.of("itemId", itemId));
    }

    public ResponseEntity<Object> getById(long userId, long itemId) {
        return get("/items/{itemId}", userId, Map.of("itemId", itemId));
    }

    public ResponseEntity<Object> getAll(long userId, int from, int size) {
        return get("/items?from={from}&size={size}", userId, Map.of("from", from, "size", size));
    }

    public ResponseEntity<Object> search(long userId, String text, int from, int size) {
        return get("/items/search?text={text}&from={from}&size={size}", userId,
                Map.of("text", text, "from", from, "size", size));
    }

    public ResponseEntity<Object> addComment(long userId, long itemId, CommentDto dto) {
        return post("/items/{itemId}/comment", userId, dto, Map.of("itemId", itemId));
    }
}
