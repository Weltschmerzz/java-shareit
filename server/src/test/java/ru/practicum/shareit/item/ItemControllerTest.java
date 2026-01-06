package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    ItemService itemService;

    @Test
    void create_shouldReturnItemDto() throws Exception {
        ItemDto in = ItemDto.builder()
                .name("Drill")
                .description("Good drill")
                .available(true)
                .build();

        ItemDto out = ItemDto.builder()
                .id(1L)
                .name("Drill")
                .description("Good drill")
                .available(true)
                .build();

        when(itemService.create(eq(1L), any(ItemDto.class))).thenReturn(out);

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(in)))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Drill"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void update_shouldReturnItemDto() throws Exception {
        ItemDto patch = ItemDto.builder()
                .name("NewName")
                .build();

        ItemDto out = ItemDto.builder()
                .id(10L)
                .name("NewName")
                .description("Desc")
                .available(true)
                .build();

        when(itemService.update(eq(1L), eq(10L), any(ItemDto.class))).thenReturn(out);

        mvc.perform(patch("/items/{itemId}", 10)
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patch)))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("NewName"));
    }

    @Test
    void getById_shouldReturnItemResponseDto() throws Exception {
        ItemResponseDto out = ItemResponseDto.builder()
                .id(10L)
                .name("Drill")
                .description("Desc")
                .available(true)
                .comments(List.of())
                .build();

        when(itemService.getById(1L, 10L)).thenReturn(out);

        mvc.perform(get("/items/{itemId}", 10)
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Drill"));
    }

    @Test
    void getOwnerItems_shouldReturnList() throws Exception {
        when(itemService.getOwnerItems(1L)).thenReturn(List.of(
                ItemResponseDto.builder().id(1L).name("A").available(true).comments(List.of()).build(),
                ItemResponseDto.builder().id(2L).name("B").available(true).comments(List.of()).build()
        ));

        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void search_shouldReturnList() throws Exception {
        when(itemService.search("dr")).thenReturn(List.of(
                ItemDto.builder().id(1L).name("Drill").description("Desc").available(true).build()
        ));

        mvc.perform(get("/items/search")
                        .param("text", "dr"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Drill"));
    }

    @Test
    void addComment_shouldReturnCommentDto() throws Exception {
        CommentCreateDto in = CommentCreateDto.builder().text("Nice").build();
        CommentDto out = CommentDto.builder().id(100L).text("Nice").authorName("Viktor").build();

        when(itemService.addComment(eq(1L), eq(10L), any(CommentCreateDto.class))).thenReturn(out);

        mvc.perform(post("/items/{itemId}/comment", 10)
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(in)))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.text").value("Nice"));
    }
}
