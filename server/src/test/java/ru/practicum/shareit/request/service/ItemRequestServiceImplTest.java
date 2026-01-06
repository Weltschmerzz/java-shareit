package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    @Mock ItemRequestRepository itemRequestRepository;
    @Mock ItemRepository itemRepository;
    @Mock UserRepository userRepository;

    ItemRequestServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ItemRequestServiceImpl(itemRequestRepository, itemRepository, userRepository);
    }

    @Test
    void create_shouldSaveRequest() {
        long userId = 1L;
        User requestor = User.builder().id(userId).name("U").email("u@mail.com").build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(requestor));

        when(itemRequestRepository.save(any(ItemRequest.class))).thenAnswer(inv -> {
            ItemRequest r = inv.getArgument(0);
            r.setId(10L);
            r.setCreated(LocalDateTime.of(2026, 1, 5, 12, 0, 0));
            return r;
        });

        ItemRequestDto out = service.create(userId, ItemRequestDto.builder().description("need drill").build());

        assertThat(out.getId()).isEqualTo(10L);
        assertThat(out.getDescription()).isEqualTo("need drill");
        assertThat(out.getItems()).isEmpty();
    }

    @Test
    void getUserRequests_shouldMapItemsAndReturnEmptyWhenNoRequests() {
        long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(User.builder().id(userId).name("U").email("u@mail.com").build()));

        when(itemRequestRepository.findAllByRequestor_IdOrderByCreatedDesc(userId)).thenReturn(List.of());
        assertThat(service.getUserRequests(userId)).isEmpty();

        ItemRequest r1 = ItemRequest.builder().id(1L).description("d1").requestor(User.builder().id(userId).build()).created(LocalDateTime.now()).build();
        ItemRequest r2 = ItemRequest.builder().id(2L).description("d2").requestor(User.builder().id(userId).build()).created(LocalDateTime.now()).build();

        when(itemRequestRepository.findAllByRequestor_IdOrderByCreatedDesc(userId)).thenReturn(List.of(r1, r2));

        Item i1 = Item.builder().id(10L).name("I1").description("x").available(true).ownerId(99L).requestId(1L).build();
        Item i2 = Item.builder().id(20L).name("I2").description("y").available(true).ownerId(98L).requestId(2L).build();
        when(itemRepository.findAllByRequestIdIn(List.of(1L, 2L))).thenReturn(List.of(i1, i2));

        var out = service.getUserRequests(userId);
        assertThat(out).hasSize(2);
        assertThat(out.get(0).getItems()).isNotNull();
    }

    @Test
    void getOtherUsersRequests_shouldValidatePaging_andMapItems() {
        long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(User.builder().id(userId).name("U").email("u@mail.com").build()));

        assertThatThrownBy(() -> service.getOtherUsersRequests(userId, -1, 10))
                .isInstanceOf(ValidationException.class);

        assertThatThrownBy(() -> service.getOtherUsersRequests(userId, 0, 0))
                .isInstanceOf(ValidationException.class);

        ItemRequest r1 = ItemRequest.builder().id(1L).description("d1").requestor(User.builder().id(2L).build()).created(LocalDateTime.now()).build();
        when(itemRequestRepository.findAllByRequestor_IdNotOrderByCreatedDesc(eq(userId), any(Pageable.class)))
                .thenReturn(List.of(r1));

        Item i1 = Item.builder().id(10L).name("I1").description("x").available(true).ownerId(99L).requestId(1L).build();
        when(itemRepository.findAllByRequestIdIn(List.of(1L))).thenReturn(List.of(i1));

        var out = service.getOtherUsersRequests(userId, 0, 10);
        assertThat(out).hasSize(1);
        assertThat(out.get(0).getItems()).hasSize(1);
    }

    @Test
    void getById_shouldReturnRequestWithItems() {
        long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(User.builder().id(userId).name("U").email("u@mail.com").build()));

        ItemRequest r = ItemRequest.builder().id(5L).description("need").requestor(User.builder().id(2L).build()).created(LocalDateTime.now()).build();
        when(itemRequestRepository.findById(5L)).thenReturn(Optional.of(r));

        Item i1 = Item.builder().id(10L).name("I1").description("x").available(true).ownerId(99L).requestId(5L).build();
        when(itemRepository.findAllByRequestId(5L)).thenReturn(List.of(i1));

        var out = service.getById(userId, 5L);
        assertThat(out.getId()).isEqualTo(5L);
        assertThat(out.getItems()).hasSize(1);
        assertThat(out.getItems().get(0).getName()).isEqualTo("I1");
    }

    @Test
    void create_whenUserNotFound_shouldThrow404() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(1L, ItemRequestDto.builder().description("x").build()))
                .isInstanceOf(NotFoundException.class);
    }
}
