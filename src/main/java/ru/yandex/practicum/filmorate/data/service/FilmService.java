package ru.yandex.practicum.filmorate.data.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.data.dto.FilmDto;
import ru.yandex.practicum.filmorate.data.exception.IdNotEmptyException;
import ru.yandex.practicum.filmorate.data.exception.NotFoundException;
import ru.yandex.practicum.filmorate.data.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.data.repository.FilmRepository;

import java.util.List;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class FilmService {
    private final FilmRepository repository;
    private final FilmMapper mapper;


    public FilmDto add(@Valid FilmDto filmDto) throws IdNotEmptyException {
        log.info("Создание фильма. Наименование: %s".formatted(filmDto.getName()));
        if (filmDto.getId() != null) {
            throw new IdNotEmptyException("При создании записи запрещена передача идентификатора");
        }
        var filmSaved = repository.addOrUpdate(mapper.toEntity(filmDto, repository.getNextId()));
        return mapper.toDto(filmSaved);
    }

    public FilmDto update(Long filmId, @Valid FilmDto filmDto) throws NotFoundException {
        log.info("Обновление фильма. Наименование: %s".formatted(filmDto.getName()));
        if (!repository.checkExist(filmId)) {
            throw new NotFoundException("Фильм не найден");
        }
        var filmSaved = repository.addOrUpdate(mapper.toEntity(filmDto, filmId));
        return mapper.toDto(filmSaved);
    }

    public List<FilmDto> getAll() {
        log.info("Получение списка всех фильмов");
        return repository.findAll()
                .stream()
                .map(mapper::toDto)
                .toList();
    }


}
