package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    UserService userService;

    @Test
    void create_shouldReturnUser() throws Exception {
        UserDto in = new UserDto(null, "Viktor", "viktor@mail.com");
        UserDto out = new UserDto(1L, "Viktor", "viktor@mail.com");

        when(userService.create(any(UserDto.class))).thenReturn(out);

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(in)))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Viktor"))
                .andExpect(jsonPath("$.email").value("viktor@mail.com"));
    }

    @Test
    void update_shouldReturnUpdatedUser() throws Exception {
        UserDto patch = new UserDto(null, "NewName", null);
        UserDto out = new UserDto(1L, "NewName", "viktor@mail.com");

        when(userService.update(eq(1L), any(UserDto.class))).thenReturn(out);

        mvc.perform(patch("/users/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patch)))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("NewName"))
                .andExpect(jsonPath("$.email").value("viktor@mail.com"));
    }

    @Test
    void getById_shouldReturnUser() throws Exception {
        UserDto out = new UserDto(1L, "Viktor", "viktor@mail.com");
        when(userService.getById(1L)).thenReturn(out);

        mvc.perform(get("/users/{id}", 1))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Viktor"))
                .andExpect(jsonPath("$.email").value("viktor@mail.com"));
    }

    @Test
    void getAll_shouldReturnList() throws Exception {
        when(userService.getAll()).thenReturn(List.of(
                new UserDto(1L, "A", "a@mail.com"),
                new UserDto(2L, "B", "b@mail.com")
        ));

        mvc.perform(get("/users"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void delete_shouldReturn2xx() throws Exception {
        doNothing().when(userService).delete(1L);

        mvc.perform(delete("/users/{id}", 1))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    void getById_whenNotFound_shouldReturn404() throws Exception {
        when(userService.getById(999L)).thenThrow(new NotFoundException("not found"));

        mvc.perform(get("/users/{id}", 999))
                .andExpect(status().isNotFound());
    }
}
