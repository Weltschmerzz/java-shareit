package ru.practicum.shareit.client;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.lang.Nullable;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RequiredArgsConstructor
public class BaseClient {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    protected final RestTemplate rest;
    protected final String serverUrl;

    protected ResponseEntity<Object> get(String path) {
        HttpEntity<Void> requestEntity = new HttpEntity<>(new HttpHeaders());
        try {
            ResponseEntity<Object> resp = rest.exchange(serverUrl + path, HttpMethod.GET, requestEntity, Object.class);
            return forward(resp);
        } catch (HttpStatusCodeException e) {
            return handleException(e);
        }
    }

    protected ResponseEntity<Object> get(String path, long userId) {
        return get(path, userId, null);
    }

    protected ResponseEntity<Object> get(String path, long userId, @Nullable Map<String, Object> params) {
        HttpEntity<Void> requestEntity = new HttpEntity<>(defaultHeaders(userId));
        try {
            ResponseEntity<Object> resp;
            if (params == null || params.isEmpty()) {
                resp = rest.exchange(serverUrl + path, HttpMethod.GET, requestEntity, Object.class);
            } else {
                resp = rest.exchange(serverUrl + path, HttpMethod.GET, requestEntity, Object.class, params);
            }
            return forward(resp);
        } catch (HttpStatusCodeException e) {
            return handleException(e);
        }
    }

    protected ResponseEntity<Object> post(String path, long userId, Object body) {
        HttpEntity<Object> requestEntity = new HttpEntity<>(body, defaultHeaders(userId));
        try {
            ResponseEntity<Object> resp = rest.exchange(serverUrl + path, HttpMethod.POST, requestEntity, Object.class);
            return forward(resp);
        } catch (HttpStatusCodeException e) {
            return handleException(e);
        }
    }

    protected ResponseEntity<Object> post(String path, long userId, Object body, Map<String, Object> params) {
        HttpEntity<Object> requestEntity = new HttpEntity<>(body, defaultHeaders(userId));
        try {
            ResponseEntity<Object> resp = rest.exchange(serverUrl + path, HttpMethod.POST, requestEntity, Object.class, params);
            return forward(resp);
        } catch (HttpStatusCodeException e) {
            return handleException(e);
        }
    }

    protected ResponseEntity<Object> patch(String path, long userId, Object body, Map<String, Object> params) {
        HttpEntity<Object> requestEntity = new HttpEntity<>(body, defaultHeaders(userId));
        try {
            ResponseEntity<Object> resp = rest.exchange(serverUrl + path, HttpMethod.PATCH, requestEntity, Object.class, params);
            return forward(resp);
        } catch (HttpStatusCodeException e) {
            return handleException(e);
        }
    }

    protected ResponseEntity<Object> patch(String path, long userId, Map<String, Object> params) {
        HttpEntity<Void> requestEntity = new HttpEntity<>(defaultHeaders(userId));
        try {
            ResponseEntity<Object> resp = rest.exchange(serverUrl + path, HttpMethod.PATCH, requestEntity, Object.class, params);
            return forward(resp);
        } catch (HttpStatusCodeException e) {
            return handleException(e);
        }
    }

    protected ResponseEntity<Object> delete(String path, long userId, Map<String, Object> params) {
        HttpEntity<Void> requestEntity = new HttpEntity<>(defaultHeaders(userId));
        try {
            ResponseEntity<Object> resp = rest.exchange(serverUrl + path, HttpMethod.DELETE, requestEntity, Object.class, params);
            return forward(resp);
        } catch (HttpStatusCodeException e) {
            return handleException(e);
        }
    }

    private HttpHeaders defaultHeaders(long userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
        if (userId > 0) {
            headers.add(USER_HEADER, String.valueOf(userId));
        }
        return headers;
    }

    private ResponseEntity<Object> forward(ResponseEntity<Object> resp) {
        MediaType contentType = resp.getHeaders().getContentType();
        if (contentType == null) {
            contentType = MediaType.APPLICATION_JSON;
        }
        return ResponseEntity.status(resp.getStatusCode())
                .contentType(contentType)
                .body(resp.getBody());
    }

    private ResponseEntity<Object> handleException(HttpStatusCodeException e) {
        MediaType contentType = null;
        HttpHeaders upstream = e.getResponseHeaders();
        if (upstream != null) {
            contentType = upstream.getContentType();
        }
        if (contentType == null) {
            contentType = MediaType.APPLICATION_JSON;
        }
        return ResponseEntity.status(e.getStatusCode())
                .contentType(contentType)
                .body(e.getResponseBodyAsString());
    }
}
