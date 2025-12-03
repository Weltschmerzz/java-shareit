package ru.practicum.shareit.user.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    private final Map<Long, User> users = new HashMap<>();
    private long seq = 0;

    @Override
    public UserDto create(UserDto dto) {
        User user = UserMapper.toUser(dto);
        user.setId(++seq);
        users.put(user.getId(), user);
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto update(Long id, UserDto dto) {
        User existing = users.get(id);
        if (existing == null) {
            throw new NotFoundException("User not found: " + id);
        }
        if (dto.getName() != null) existing.setName(dto.getName());
        if (dto.getEmail() != null) existing.setEmail(dto.getEmail());
        return UserMapper.toUserDto(existing);
    }

    @Override
    public UserDto getById(Long id) {
        User user = users.get(id);
        if (user == null) throw new NotFoundException("User not found: " + id);
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAll() {
        return users.values().stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    @Override
    public void delete(Long id) {
        users.remove(id);
    }
}
