package ru.yandex.practicum.filmorate.data.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.data.dto.UserDto;
import ru.yandex.practicum.filmorate.data.exception.ConditionsException;
import ru.yandex.practicum.filmorate.data.exception.NotFoundException;
import ru.yandex.practicum.filmorate.data.mapper.UserMapper;
import ru.yandex.practicum.filmorate.data.repository.UserRepository;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repository;
    private final UserMapper mapper;

    public UserDto add(@Valid UserDto userDto) throws ConditionsException {
        log.info("Создание пользователя (старт). Логин: {}", userDto.getLogin());
        if (userDto.getId() != null) {
            throw new ConditionsException("При создании записи запрещена передача идентификатора");
        }
        prepareDto(userDto);
        var userSaved = repository.addOrUpdate(mapper.toEntity(userDto, repository.getNextId()));
        log.info("Создание пользователя (стоп). Логин: {}", userDto.getLogin());
        return mapper.toDto(userSaved);
    }

    public UserDto update(Long userId, @Valid UserDto userDto) throws NotFoundException {
        log.info("Обновление пользователя (старт). Логин: {}", userDto.getLogin());
        if (!repository.checkExist(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }
        prepareDto(userDto);
        var userSaved = repository.addOrUpdate(mapper.toEntity(userDto, userId));
        log.info("Обновление пользователя (стоп). Логин: {}", userDto.getLogin());
        return mapper.toDto(userSaved);
    }

    public void prepareDto(UserDto userDto) {
        if (StringUtils.isBlank(userDto.getName())) {
            log.debug("id:{} -> пустой name заменен на Login", userDto.getId());
            userDto.setName(userDto.getLogin());
        }
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
        return mapper.toDto(repository.findById(userId));
    }

    public void addFriend(Long id, Long friendId) throws NotFoundException, ConditionsException {
        log.info("Добавление в друзья (старт). кто: {} к кому: {} ", friendId, id);
        if (!repository.checkExist(id)) {
            throw new NotFoundException("Пользователь к кому добавляется в друзья не найден");
        }
        if (!repository.checkExist(friendId)) {
            throw new NotFoundException("Пользователь который добавляется в друзья не найден");
        }
        if (Objects.equals(id, friendId)) {
            throw new ConditionsException("Нельзя самому к себе добавится в друзья");
        }
        var user = repository.findById(id);
        var friend = repository.findById(friendId);
        if (user.isFriend(friend)) {
            throw new ConditionsException("Пользователь уже в списке друзей");
        }
        user.addFriend(friend);
        friend.addFriend(user);
        log.info("Добавление в друзья (стоп). кто: {} к кому: {} ", friendId, id);
    }

    public void removeFriend(Long id, Long friendId) throws NotFoundException {
        log.info("Удаление из друзей (старт). кто: {} от кого: {} ", friendId, id);
        if (!repository.checkExist(id)) {
            throw new NotFoundException("Пользователь к кому добавляется в друзья не найден");
        }
        if (!repository.checkExist(friendId)) {
            throw new NotFoundException("Пользователь который добавляется в друзья не найден");
        }
        var user = repository.findById(id);
        var friend = repository.findById(friendId);

//        if (!user.isFriend(friend)) {
//            throw new ConditionsException("Пользователь не найден в списке друзей");
//        }

        user.removeFriend(friend);
        friend.removeFriend(user);
        log.info("Удаление из друзей (старт). кто: {} от кого: {} ", friendId, id);
    }

    public List<UserDto> userFriends(Long id) throws NotFoundException {
        log.info("Список друзей (старт): {} ", id);
        if (!repository.checkExist(id)) {
            throw new NotFoundException("Пользователь к кому добавляется в друзья не найден");
        }
        var user = repository.findById(id);
        log.info("Список друзей (стоп): {} ", id);
        return user.getFriends()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    public List<UserDto> commonFriends(Long id, Long otherId) throws NotFoundException, ConditionsException {
        log.info("Общие друзья пользователей (старт). пользователь 1: {} пользователь 2: {} ", id, otherId);
        if (!repository.checkExist(id)) {
            throw new NotFoundException("Пользователь к кому добавляется в друзья не найден");
        }
        if (!repository.checkExist(otherId)) {
            throw new NotFoundException("Пользователь который добавляется в друзья не найден");
        }
        if (Objects.equals(id, otherId)) {
            throw new ConditionsException("Нельзя самому к себе добавится в друзья");
        }
        var user = repository.findById(id);
        var userOther = repository.findById(otherId);

        log.info("Общие друзья пользователей (стоп). пользователь 1: {} пользователь 2: {} ", id, otherId);
        return user.getCommonFriends(userOther)
                .stream()
                .map(mapper::toDto)
                .toList();
    }
}
