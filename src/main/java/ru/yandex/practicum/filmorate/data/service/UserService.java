package ru.yandex.practicum.filmorate.data.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.data.dto.UserDto;
import ru.yandex.practicum.filmorate.data.exception.ConditionsException;
import ru.yandex.practicum.filmorate.data.exception.NotFoundException;
import ru.yandex.practicum.filmorate.data.mapper.UserMapper;
import ru.yandex.practicum.filmorate.data.model.User;
import ru.yandex.practicum.filmorate.data.repository.UserRepository;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repository;
    private final UserMapper mapper;

    public UserDto insert(@Valid UserDto userDto) throws ConditionsException {
        log.info("Создание пользователя (старт). Логин: {}", userDto.getLogin());
        User user = mapper.toEntity(userDto);
        user = repository.insert(user);
        log.info("Создание пользователя (стоп). Логин: {}", userDto.getLogin());
        return mapper.toDto(user);
    }

    public UserDto update(Long userId, @Valid UserDto userDto) throws ConditionsException, NotFoundException {
        log.info("Обновление пользователя (старт). Логин: {}", userDto.getLogin());
        var user = repository.findByIdOrThrow(userId);
        mapper.map(user, userDto);
        user = repository.update(user);
        log.info("Обновление пользователя (стоп). Логин: {}", userDto.getLogin());
        return mapper.toDto(user);
    }

    public List<UserDto> getAll() {
        log.info("Получение списка всех пользователей (старт)");
        var result = repository.findAll()
                .stream()
                .map(mapper::toDto)
                .toList();
        log.info("Получение списка всех пользователей (стоп)");
        return result;
    }

    public UserDto getUser(Long userId) throws NotFoundException {
        return mapper.toDto(repository.findByIdOrThrow(userId));
    }


    public void addFriend(Long userId, Long friendId) throws NotFoundException, ConditionsException {
        log.info("Добавление в друзья (старт). кто: {} к кому: {}", friendId, userId);
        repository.findByIdOrThrow(userId);
        repository.findByIdOrThrow(friendId);

        if (Objects.equals(userId, friendId)) {
            throw new ConditionsException("Нельзя добавить самого себя в друзья");
        }

        // Добавляем дружбу (статус false - запрос на дружбу)
        repository.addFriend(userId, friendId, false);
        log.info("Добавление в друзья (стоп). кто: {} к кому: {}", friendId, userId);
    }

    public void removeFriend(Long userId, Long friendId) throws NotFoundException {
        log.info("Удаление из друзей (старт). кто: {} от кого: {}", friendId, userId);

        // Проверка существования пользователей
        repository.findByIdOrThrow(userId);
        repository.findByIdOrThrow(friendId);

        repository.removeFriend(userId, friendId);
        log.info("Удаление из друзей (стоп). кто: {} от кого: {}", friendId, userId);
    }

    public List<UserDto> getUserFriends(Long userId) throws NotFoundException {
        log.info("Список друзей (старт): {}", userId);
        repository.findByIdOrThrow(userId);
        List<UserDto> friends = repository.getFriends(userId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());

        log.info("Список друзей (стоп): {}", userId);
        return friends;
    }

    public List<UserDto> getCommonFriends(Long userId, Long otherId) throws NotFoundException, ConditionsException {
        log.info("Общие друзья пользователей (старт). пользователь 1: {} пользователь 2: {}", userId, otherId);

        repository.findByIdOrThrow(userId);
        repository.findByIdOrThrow(otherId);

        if (Objects.equals(userId, otherId)) {
            throw new ConditionsException("Нельзя искать общих друзей у одного пользователя");
        }

        List<UserDto> commonFriends = repository.getCommonFriends(userId, otherId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());

        log.info("Общие друзья пользователей (стоп). пользователь 1: {} пользователь 2: {}", userId, otherId);
        return commonFriends;
    }
}
