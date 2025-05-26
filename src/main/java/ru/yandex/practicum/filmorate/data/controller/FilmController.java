package ru.yandex.practicum.filmorate.data.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.data.dto.FilmDto;
import ru.yandex.practicum.filmorate.data.exception.IdNotEmptyException;
import ru.yandex.practicum.filmorate.data.exception.NotFoundException;
import ru.yandex.practicum.filmorate.data.service.FilmService;

import java.util.List;

@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {
    private final FilmService filmService;

    @PostMapping
    public FilmDto addFilm(@RequestBody FilmDto filmDto) throws IdNotEmptyException {
        return filmService.add(filmDto);
    }

    @PutMapping("/{filmId}")
    public FilmDto updateFilm(@PathVariable Long filmId, @RequestBody FilmDto filmDto) throws NotFoundException {
        return filmService.update(filmId,filmDto);
    }

    @PutMapping
    public FilmDto updateFilm(@RequestBody FilmDto filmDto) throws NotFoundException {
        return filmService.update(filmDto.getId(),filmDto);
    }

    @GetMapping
    public List<FilmDto> getFilms() {
        return filmService.getAll();
    }
}
