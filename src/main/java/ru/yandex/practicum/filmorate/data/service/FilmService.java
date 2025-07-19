package ru.yandex.practicum.filmorate.data.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.data.constant.DirectorSortValue;
import ru.yandex.practicum.filmorate.data.constant.EventType;
import ru.yandex.practicum.filmorate.data.constant.Operation;
import ru.yandex.practicum.filmorate.data.dto.FilmDto;
import ru.yandex.practicum.filmorate.data.dto.GenreDto;
import ru.yandex.practicum.filmorate.data.exception.ConditionsException;
import ru.yandex.practicum.filmorate.data.exception.NotFoundException;
import ru.yandex.practicum.filmorate.data.mapper.DirectorMapper;
import ru.yandex.practicum.filmorate.data.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.data.model.Film;
import ru.yandex.practicum.filmorate.data.repository.DirectorRepository;
import ru.yandex.practicum.filmorate.data.repository.FilmRepository;
import ru.yandex.practicum.filmorate.data.repository.GenreRepository;
import ru.yandex.practicum.filmorate.data.repository.MpaRatingRepository;
import ru.yandex.practicum.filmorate.data.repository.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class FilmService {

    private final FilmRepository repository;
    private final UserRepository repositoryUser;
    private final FilmMapper mapper;
    private final DirectorMapper mapperDirector;
    private final MpaRatingRepository repositoryMpaRating;
    private final GenreRepository repositoryGenre;
    private final DirectorRepository repositoryDirector;
    private final EventLogService eventLogService;


    public FilmDto add(@Valid FilmDto filmDto) throws ConditionsException, NotFoundException {
        log.info("Создание фильма (старт). Наименование: {}", filmDto.getName());
        validateFilmRelations(filmDto);
        Film film = mapper.toEntity(filmDto);
        film = repository.insert(film);
        log.info("Создание фильма (стоп). Наименование: {}", filmDto.getName());
        return mapper.toDto(film);
    }

    public FilmDto update(Long filmId, @Valid FilmDto filmDto) throws NotFoundException, ConditionsException {
        log.info("Обновление фильма (старт). Наименование: {}", filmDto.getName());
        validateFilmRelations(filmDto);
        var film = repository.findByIdOrThrow(filmId);
        film = mapper.map(film, filmDto);
        film = repository.update(film);
        log.info("Обновление фильма (стоп). Наименование: {}", filmDto.getName());
        return mapper.toDto(film);
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
        return mapper.toDto(repository.findByIdOrThrow(filmId));
    }

    public void remove(Long filmId) throws NotFoundException {
        log.info("Удаление фильма (старт). Фильм: {}", filmId);
        repository.deleteById(filmId);
        log.info("Удаление фильма (стоп). Фильм: {}", filmId);
    }

    public void addLike(Long filmId, Long userId) throws NotFoundException, ConditionsException {
        log.info("Добавить лайк фильму (старт). Пользователь: {}, фильм: {}", userId, filmId);
        validateExistsFilmAndUser(filmId, userId);
        repository.addLike(filmId, userId);
        eventLogService.add(userId, filmId, EventType.LIKE, Operation.ADD);
        log.info("Добавить лайк фильму (стоп). Пользователь: {}, фильм: {}", userId, filmId);
    }

    public void removeLike(Long filmId, Long userId) throws NotFoundException, ConditionsException {
        log.info("Удалить лайк (старт). Пользователь: {}, фильм: {}", userId, filmId);
        validateExistsFilmAndUser(filmId, userId);
        repository.removeLike(filmId, userId);
        eventLogService.add(userId, filmId, EventType.LIKE, Operation.REMOVE);
        log.info("Удалить лайк (стоп). Пользователь: {}, фильм: {}", userId, filmId);
    }

    public List<FilmDto> getPopularFilms(Long count, Integer year, Long genreId) {
        log.info("Получить список популярных фильмов (старт). Лимит: {}", count);
        var result = repository.getPopularFilms(count, year, genreId)
                .stream()
                .map(mapper::toDto)
                .toList();
        log.info("Получить список популярных фильмов (стоп). Лимит: {}", count);
        return result;
    }

    private void validateFilmRelations(FilmDto filmDto) throws NotFoundException {
        // Проверка MPA
        if (filmDto.getMpa() == null || filmDto.getMpa().getId() == null) {
            throw new NotFoundException("MPA рейтинг должен быть указан");
        }
        if (!repositoryMpaRating.existsById(filmDto.getMpa().getId())) {
            throw new NotFoundException("Рейтинг MPA с ID " + filmDto.getMpa().getId() + " не найден");
        }
        // Проверка жанров
        if (filmDto.getGenres() != null && !filmDto.getGenres().isEmpty()) {
            List<Long> genreIds = filmDto.getGenres().stream()
                    .map(GenreDto::getId)
                    .collect(Collectors.toList());
            if (!repositoryGenre.existAllByIds(genreIds)) {
                throw new NotFoundException("Один или несколько жанров не найдены");
            }
        }
        // Проверка режиссеров
        var film = mapper.toEntity(filmDto);
        if (!repositoryDirector.isExistAllDirectors(film.getDirectors())) {
            throw new NotFoundException("Один или несколько режиссёров не найдены");
        }
    }

    private void validateExistsFilmAndUser(Long filmId, Long userId) throws NotFoundException {
        repositoryUser.checkExists(userId);
        repository.checkExists(filmId);
    }

    private void validateDirectors(Long directorId) throws NotFoundException {
        repositoryDirector.checkExists(directorId);
    }

    private void validateCommonFilms(Long userId, Long friendId) throws NotFoundException, ConditionsException {
        if (Objects.equals(userId, friendId)) {
            throw new ConditionsException("Нельзя искать общие фильмы у одного пользователя");
        }
        repositoryUser.checkExists(userId);
        repositoryUser.checkExists(friendId);
    }

    public List<FilmDto> findByDirector(Long directorId, DirectorSortValue sortBy) throws NotFoundException {
        log.info("Запрос списка фильмов режиссёра id = {} sortBy = {}", directorId, sortBy);
        validateDirectors(directorId);
        return repository.getFilmByDirector(directorId, sortBy)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    public List<FilmDto> find(String query, String by) {
        if (query == null || query.isBlank() || by == null || by.isBlank()) {
            return Collections.emptyList();
        }

        String[] searchParams = by.split(",");
        List<Film> films = repository.searchFilms(query.trim(), searchParams);

        return films.stream()
                .distinct()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    public List<FilmDto> getCommonFilms(Long userId, Long friendId) throws NotFoundException, ConditionsException {
        log.info("Общие фильмы пользователей (старт). пользователь 1: {} пользователь 2: {}", userId, friendId);
        validateCommonFilms(userId, friendId);
        List<Film> films = repository.getCommonFilms(userId, friendId);
        log.info("Общие фильмы пользователей (стоп). пользователь 1: {} пользователь 2: {}", userId, friendId);
        return films.stream()
                .distinct()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }
}
