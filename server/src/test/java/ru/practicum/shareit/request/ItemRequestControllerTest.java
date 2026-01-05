package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestItemDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    ItemRequestService itemRequestService;

    @Test
    void create_shouldReturnRequest() throws Exception {
        ItemRequestDto in = ItemRequestDto.builder()
                .description("Need drill")
                .build();

        ItemRequestDto out = ItemRequestDto.builder()
                .id(1L)
                .description("Need drill")
                .created(LocalDateTime.now())
                .items(List.of())
                .build();

        when(itemRequestService.create(eq(1L), any(ItemRequestDto.class))).thenReturn(out);

        mvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(in)))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Need drill"));
    }

    @Test
    void getUserRequests_shouldReturnList() throws Exception {
        when(itemRequestService.getUserRequests(1L)).thenReturn(List.of(
                ItemRequestDto.builder()
                        .id(1L)
                        .description("Need drill")
                        .created(LocalDateTime.now())
                        .items(List.of(
                                ItemRequestItemDto.builder().id(10L).name("Drill").ownerId(2L).build()
                        ))
                        .build()
        ));

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].items[0].name").value("Drill"));
    }

    @Test
    void getOtherUsersRequests_shouldReturnList() throws Exception {
        when(itemRequestService.getOtherUsersRequests(eq(1L), eq(0), eq(10))).thenReturn(List.of(
                ItemRequestDto.builder().id(2L).description("Need ladder").created(LocalDateTime.now()).items(List.of()).build()
        ));

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", "1")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(2));
    }

    @Test
    void getById_shouldReturnRequest() throws Exception {
        ItemRequestDto out = ItemRequestDto.builder()
                .id(1L)
                .description("Need drill")
                .created(LocalDateTime.now())
                .items(List.of(
                        ItemRequestItemDto.builder().id(10L).name("Drill").ownerId(2L).build()
                ))
                .build();

        when(itemRequestService.getById(eq(1L), eq(1L))).thenReturn(out);

        mvc.perform(get("/requests/{requestId}", 1)
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.items[0].id").value(10))
                .andExpect(jsonPath("$.items[0].ownerId").value(2));
    }
}
