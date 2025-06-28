package ru.yandex.practicum.filmorate.data.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.data.dto.FilmDto;
import ru.yandex.practicum.filmorate.data.exception.ConditionsException;
import ru.yandex.practicum.filmorate.data.exception.NotFoundException;
import ru.yandex.practicum.filmorate.data.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.data.repository.FilmRepository;
import ru.yandex.practicum.filmorate.data.repository.UserRepository;

import java.util.List;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class FilmService {
    private final FilmRepository repository;
    private final UserRepository repositoryUser;
    private final FilmMapper mapper;


    public FilmDto add(@Valid FilmDto filmDto) throws ConditionsException {
        log.info("Создание фильма (старт). Наименование: {}", filmDto.getName());
        if (filmDto.getId() != null) {
            throw new ConditionsException("При создании записи запрещена передача идентификатора");
        }
        var filmSaved = repository.addOrUpdate(mapper.map(filmDto, repository.getNextId()));
        log.info("Создание фильма (стоп). Наименование: {}", filmDto.getName());
        return mapper.toDto(filmSaved);
    }

    public FilmDto update(Long filmId, @Valid FilmDto filmDto) throws NotFoundException {
        log.info("Обновление фильма (старт). Наименование: {}", filmDto.getName());
        var film = repository.findById(filmId);
        mapper.map(film, filmDto);
        var filmSaved = repository.addOrUpdate(film);
        log.info("Обновление фильма (стоп). Наименование: {}", filmDto.getName());
        return mapper.toDto(filmSaved);
    }

    public List<FilmDto> getAll() {
        log.info("Получение списка всех фильмов (старт)");
        var result = repository.findAll()
                .stream()
                .map(mapper::toDto)
                .toList();
        log.info("Получение списка всех фильмов (стоп)");
        return result;
    }

    public FilmDto getFilms(Long filmId) throws NotFoundException {
        return mapper.toDto(repository.findById(filmId));
    }

    public void addLike(Long filmId, Long userId) throws NotFoundException, ConditionsException {
        log.info("Добавить лайк фильму (старт). Пользователь: {}, фильм: {}", userId, filmId);
        if (!repositoryUser.checkExist(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }
        var film = repository.findById(filmId);
        if (film.isLiked(userId)) {
            throw new ConditionsException("Лайк уже учтен");
        }
        film.addLike(userId);
        log.info("Добавить лайк фильму (стоп). Пользователь: {}, фильм: {}", userId, filmId);
    }

    public void removeLike(Long filmId, Long userId) throws NotFoundException, ConditionsException {
        log.info("Удалить лайк (старт). Пользователь: {}, фильм: {}", userId, filmId);
        if (!repositoryUser.checkExist(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }
        var film = repository.findById(filmId);
        if (!film.isLiked(userId)) {
            throw new ConditionsException("Пользователь не может удалить лайк который не добавлял");
        }
        film.removeLike(userId);
        log.info("Удалить лайк (стоп). Пользователь: {}, фильм: {}", userId, filmId);
    }

    public List<FilmDto> findPopular(Long limit) {
        log.info("Популярные фильмы. Лимит: {}", limit);
        return repository.findPopular(limit)
                .stream()
                .map(mapper::toDto)
                .toList();
    }
}
