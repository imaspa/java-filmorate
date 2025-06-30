package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.filmorate.data.exception.NotFoundException;
import ru.yandex.practicum.filmorate.data.model.Genre;
import ru.yandex.practicum.filmorate.data.repository.GenreRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(GenreRepository.class)
class GenreRepositoryIT {

    @Autowired
    private GenreRepository genreRepository;

    @Test
    void shouldFindAllGenres() {
        List<Genre> genres = genreRepository.findAll();
        assertThat(genres).hasSize(6); // Из data.sql
        assertThat(genres.get(0).getName()).isEqualTo("Комедия");
    }

    @Test
    void shouldFindGenreById() throws NotFoundException {
        Genre genre = genreRepository.findByIdOrThrow(1L);
        assertThat(genre.getName()).isEqualTo("Комедия");
    }

    @Test
    void shouldCheckIfGenreExists() {
        assertThat(genreRepository.existsById(1L)).isTrue();
        assertThat(genreRepository.existsById(999L)).isFalse();
    }

    @Test
    void shouldCheckIfAllGenresExist() {
        assertThat(genreRepository.existAllByIds(List.of(1L, 2L))).isTrue();
        assertThat(genreRepository.existAllByIds(List.of(1L, 999L))).isFalse();
    }
}
