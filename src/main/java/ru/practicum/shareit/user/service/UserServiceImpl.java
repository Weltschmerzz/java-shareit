package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.NotUniqueInputValue;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDto create(UserDto dto) {
        if (userRepository.existsByEmailIgnoreCase(dto.getEmail())) {
            throw new NotUniqueInputValue("Пользователь с email " + dto.getEmail() + " уже существует");
        }

        User user = UserMapper.toUser(dto);
        User saved = userRepository.save(user);

        return UserMapper.toUserDto(saved);
    }

    @Override
    @Transactional
    public UserDto update(Long id, UserDto dto) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + id));

        if (dto.getEmail() != null) {
            String newEmail = dto.getEmail();
            String oldEmail = existing.getEmail();

            if (!newEmail.equalsIgnoreCase(oldEmail) && userRepository.existsByEmailIgnoreCase(newEmail)) {
                throw new NotUniqueInputValue("Пользователь с email " + newEmail + " уже существует");
            }

            existing.setEmail(newEmail);
        }

        if (dto.getName() != null) {
            existing.setName(dto.getName());
        }

        User saved = userRepository.save(existing);

        return UserMapper.toUserDto(saved);
    }

    @Override
    public UserDto getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + id));

        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAll() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        userRepository.deleteById(id);
    }
}
