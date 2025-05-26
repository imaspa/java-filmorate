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
        log.info("Создание пользователя. Логин: %s".formatted(userDto.getLogin()));
        if (userDto.getId() != null) {
            throw new IdNotEmptyException("При создании записи запрещена передача идентификатора");
        }
        prepareDto(userDto);
        var filmSaved = repository.addOrUpdate(mapper.toEntity(userDto,repository.getNextId()));
        return mapper.toDto(filmSaved);
    }

    public UserDto update(Long userId, @Valid UserDto userDto) throws NotFoundException {
        log.info("Обновление пользователя. Логин: %s".formatted(userDto.getLogin()));
        if (!repository.checkExist(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }
        prepareDto(userDto);
        var filmSaved = repository.addOrUpdate(mapper.toEntity(userDto,userId));
        return mapper.toDto(filmSaved);
    }

    public void prepareDto(UserDto userDto) {
        if (StringUtils.isBlank(userDto.getName())) {
            log.debug("id:%s -> пустой name заменен на Login".formatted(String.valueOf(userDto.getId())));
            userDto.setName(userDto.getLogin());
        }
    }

    public List<UserDto> getAll() {
        log.info("Получение списка всех пользователей");
        return repository.findAll()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

}
