package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ItemServiceIT {

    @Autowired
    UserService userService;

    @Autowired
    ItemService itemService;

    @Test
    void getOwnerItems_shouldReturnOnlyOwnerItems() {
        UserDto owner = userService.create(UserDto.builder()
                .name("Owner")
                .email("owner@mail.com")
                .build());

        UserDto other = userService.create(UserDto.builder()
                .name("Other")
                .email("other@mail.com")
                .build());

        itemService.create(owner.getId(), ItemDto.builder()
                .name("OwnerItem")
                .description("desc")
                .available(true)
                .build());

        itemService.create(other.getId(), ItemDto.builder()
                .name("OtherItem")
                .description("desc")
                .available(true)
                .build());

        List<ItemResponseDto> result = itemService.getOwnerItems(owner.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("OwnerItem");
    }
}
