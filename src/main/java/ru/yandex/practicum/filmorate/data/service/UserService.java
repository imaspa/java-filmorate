package ru.yandex.practicum.filmorate.data.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.data.dto.UserDto;
import ru.yandex.practicum.filmorate.data.exception.IdNotEmptyException;
import ru.yandex.practicum.filmorate.data.exception.NotFoundException;
import ru.yandex.practicum.filmorate.data.mapper.UserMapper;
import ru.yandex.practicum.filmorate.data.repository.UserRepository;

import java.util.List;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repository;
    private final UserMapper mapper;

    public UserDto add(@Valid UserDto userDto) throws IdNotEmptyException {
        log.info("Создание пользователя (старт). Логин: {}", userDto.getLogin());
        if (userDto.getId() != null) {
            throw new IdNotEmptyException("При создании записи запрещена передача идентификатора");
        }
        prepareDto(userDto);
        var filmSaved = repository.addOrUpdate(mapper.toEntity(userDto, repository.getNextId()));
        log.info("Создание пользователя (стоп). Логин: {}", userDto.getLogin());
        return mapper.toDto(filmSaved);
    }

    public UserDto update(Long userId, @Valid UserDto userDto) throws NotFoundException {
        log.info("Обновление пользователя (старт). Логин: {}", userDto.getLogin());
        if (!repository.checkExist(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }
        prepareDto(userDto);
        var filmSaved = repository.addOrUpdate(mapper.toEntity(userDto, userId));
        log.info("Обновление пользователя (стоп). Логин: {}", userDto.getLogin());
        return mapper.toDto(filmSaved);
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

}
