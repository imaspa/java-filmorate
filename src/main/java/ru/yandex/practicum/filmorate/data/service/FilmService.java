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
import ru.yandex.practicum.filmorate.data.model.Film;
import ru.yandex.practicum.filmorate.data.model.Genre;
import ru.yandex.practicum.filmorate.data.repository.FilmRepository;
import ru.yandex.practicum.filmorate.data.repository.GenreRepository;
import ru.yandex.practicum.filmorate.data.repository.MpaRatingRepository;
import ru.yandex.practicum.filmorate.data.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class FilmService {

    private final FilmRepository repository;
    private final UserRepository repositoryUser;
    private final FilmMapper mapper;
    private final MpaRatingRepository repositoryMpaRating;
    private final GenreRepository repositoryGenre;


    public FilmDto add(@Valid FilmDto filmDto) throws ConditionsException, NotFoundException {
        log.info("Создание фильма (старт). Наименование: {}", filmDto.getName());
        Film film = mapper.toEntity(filmDto);
        validateFilmRelations(film);
        film = repository.insert(film);
        log.info("Создание фильма (стоп). Наименование: {}", filmDto.getName());
        return mapper.toDto(film);
    }

    public FilmDto update(Long filmId, @Valid FilmDto filmDto) throws NotFoundException, ConditionsException {
        log.info("Обновление фильма (старт). Наименование: {}", filmDto.getName());
        var film = mapper.map(repository.findByIdOrThrow(filmId), filmDto);
        validateFilmRelations(film);
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

    public void addLike(Long filmId, Long userId) throws NotFoundException, ConditionsException {
        log.info("Добавить лайк фильму (старт). Пользователь: {}, фильм: {}", userId, filmId);
        repositoryUser.findByIdOrThrow(userId);
        repository.findByIdOrThrow(filmId);
        repository.addLike(filmId, userId);
        log.info("Добавить лайк фильму (стоп). Пользователь: {}, фильм: {}", userId, filmId);
    }

    public void removeLike(Long filmId, Long userId) throws NotFoundException, ConditionsException {
        log.info("Удалить лайк (старт). Пользователь: {}, фильм: {}", userId, filmId);
        repositoryUser.findByIdOrThrow(userId);
        repository.findByIdOrThrow(filmId);

        repository.removeLike(filmId, userId);
        log.info("Удалить лайк (стоп). Пользователь: {}, фильм: {}", userId, filmId);
    }

    public List<FilmDto> findPopular(Long limit) {
        log.info("Популярные фильмы. Лимит: {}", limit);
        return repository.getPopularFilms(limit)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    private void validateFilmRelations(Film film) throws NotFoundException {
        // Проверка MPA
        if (film.getMpa() == null || film.getMpa().getId() == null) {
            throw new NotFoundException("MPA рейтинг должен быть указан");
        }
        if (!repositoryMpaRating.existsById(film.getMpa().getId())) {
            throw new NotFoundException("Рейтинг MPA с ID " + film.getMpa().getId() + " не найден");
        }

        // Проверка жанров
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            List<Long> genreIds = film.getGenres().stream()
                    .map(Genre::getId)
                    .collect(Collectors.toList());

            if (!repositoryGenre.existAllByIds(genreIds)) {
                throw new NotFoundException("Один или несколько жанров не найдены");
            }
        }
    }
}
