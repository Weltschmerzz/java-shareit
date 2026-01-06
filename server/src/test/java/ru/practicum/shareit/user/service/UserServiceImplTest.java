package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.NotUniqueInputValue;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    UserRepository userRepository;

    UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository);
    }

    @Test
    void create_whenEmailAlreadyExists_shouldThrow409() {
        UserDto in = UserDto.builder().name("A").email("a@mail.com").build();
        when(userRepository.existsByEmailIgnoreCase("a@mail.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(in))
                .isInstanceOf(NotUniqueInputValue.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void create_shouldSaveAndReturnDto() {
        UserDto in = UserDto.builder().name("A").email("a@mail.com").build();
        when(userRepository.existsByEmailIgnoreCase("a@mail.com")).thenReturn(false);

        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        UserDto out = userService.create(in);

        assertThat(out.getId()).isEqualTo(1L);
        assertThat(out.getName()).isEqualTo("A");
        assertThat(out.getEmail()).isEqualTo("a@mail.com");
    }

    @Test
    void update_whenUserNotFound_shouldThrow404() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.update(99L, UserDto.builder().name("X").build()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void update_whenEmailChangedToExisting_shouldThrow409() {
        User existing = User.builder().id(1L).name("A").email("old@mail.com").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmailIgnoreCase("new@mail.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.update(1L, UserDto.builder().email("new@mail.com").build()))
                .isInstanceOf(NotUniqueInputValue.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void update_whenEmailSameIgnoringCase_shouldNotCheckUniqueness() {
        User existing = User.builder().id(1L).name("A").email("TeSt@mail.com").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserDto out = userService.update(1L, UserDto.builder().email("test@mail.com").build());

        assertThat(out.getEmail()).isEqualTo("test@mail.com");
        verify(userRepository, never()).existsByEmailIgnoreCase(anyString());
    }

    @Test
    void update_shouldPatchFieldsAndSave() {
        User existing = User.builder().id(1L).name("Old").email("old@mail.com").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmailIgnoreCase("new@mail.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserDto patch = UserDto.builder().name("New").email("new@mail.com").build();
        UserDto out = userService.update(1L, patch);

        assertThat(out.getName()).isEqualTo("New");
        assertThat(out.getEmail()).isEqualTo("new@mail.com");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("New");
        assertThat(captor.getValue().getEmail()).isEqualTo("new@mail.com");
    }

    @Test
    void getById_shouldReturnDto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(
                User.builder().id(1L).name("A").email("a@mail.com").build()
        ));

        UserDto out = userService.getById(1L);

        assertThat(out.getId()).isEqualTo(1L);
        assertThat(out.getName()).isEqualTo("A");
    }

    @Test
    void getAll_shouldMapToDtos() {
        when(userRepository.findAll()).thenReturn(List.of(
                User.builder().id(1L).name("A").email("a@mail.com").build(),
                User.builder().id(2L).name("B").email("b@mail.com").build()
        ));

        List<UserDto> out = userService.getAll();

        assertThat(out).hasSize(2);
        assertThat(out).extracting(UserDto::getId).containsExactly(1L, 2L);
    }

    @Test
    void delete_shouldCallRepository() {
        userService.delete(10L);
        verify(userRepository).deleteById(10L);
    }
}
