package ru.yandex.practicum.filmorate.helper;

import ru.yandex.practicum.filmorate.data.dto.FilmDto;

import java.time.LocalDate;

public final class FilmUtils {
    public static FilmDto createFilmValid() {

        return FilmDto.builder()
                .name("Название фильма")
                .description("Описание фильма")
                .releaseDate(LocalDate.of(1980, 1, 1))
                .duration(1000)
                .build();
    }

    public static FilmDto createFilmNameEmpty() {
        return createFilmValid().toBuilder()
                .name("")
                .build();
    }

    public static FilmDto createFilmDescriptionGT200() {
        return createFilmValid().toBuilder()
                .description("X".repeat(201))
                .build();
    }

    public static FilmDto createFilmReleaseDateEmpty() {
        return createFilmValid().toBuilder()
                .releaseDate(null)
                .build();
    }

    public static FilmDto createFilmReleaseDateBefore1895_12_28() {
        return createFilmValid().toBuilder()
                .releaseDate(LocalDate.of(1895, 1, 1))
                .build();
    }

    public static FilmDto createFilmDurationZero() {
        return createFilmValid().toBuilder()
                .duration(0)
                .build();
    }
}