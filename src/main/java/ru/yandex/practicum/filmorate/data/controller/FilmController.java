package ru.yandex.practicum.filmorate.data.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.data.dto.FilmDto;
import ru.yandex.practicum.filmorate.data.exception.ConditionsException;
import ru.yandex.practicum.filmorate.data.exception.NotFoundException;
import ru.yandex.practicum.filmorate.data.service.FilmService;

import java.util.List;

@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {
    private final FilmService filmService;

    @PostMapping
    public FilmDto addFilm(@RequestBody FilmDto filmDto) throws ConditionsException, NotFoundException {
        return filmService.add(filmDto);
    }

    @PutMapping("/{filmId}")
    public FilmDto updateFilm(@PathVariable Long filmId, @RequestBody FilmDto filmDto) throws ConditionsException, NotFoundException {
        return filmService.update(filmId, filmDto);
    }

    @PutMapping
    public FilmDto updateFilm(@RequestBody FilmDto filmDto) throws NotFoundException, ConditionsException {
        return filmService.update(filmDto.getId(), filmDto);
    }

    @GetMapping
    public List<FilmDto> getFilms() {
        return filmService.getAll();
    }

    @GetMapping("/{filmId}")
    public FilmDto getFilms(@PathVariable Long filmId) throws NotFoundException {
        return filmService.getFilms(filmId);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) throws ConditionsException, NotFoundException {
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable Long id, @PathVariable Long userId) throws ConditionsException, NotFoundException {
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public List<FilmDto> findPopular(@RequestParam(required = false, defaultValue = "10") Long count) {
        return filmService.findPopular(count);
    }
}
