package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.filmorate.data.exception.ConditionsException;
import ru.yandex.practicum.filmorate.data.exception.NotFoundException;
import ru.yandex.practicum.filmorate.data.model.Film;
import ru.yandex.practicum.filmorate.data.model.Genre;
import ru.yandex.practicum.filmorate.data.model.MpaRating;
import ru.yandex.practicum.filmorate.data.model.User;
import ru.yandex.practicum.filmorate.data.repository.FilmRepository;
import ru.yandex.practicum.filmorate.data.repository.GenreRepository;
import ru.yandex.practicum.filmorate.data.repository.MpaRatingRepository;
import ru.yandex.practicum.filmorate.data.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import({FilmRepository.class, UserRepository.class, MpaRatingRepository.class, GenreRepository.class})
class FilmRepositoryIT {

    @Autowired
    private FilmRepository filmRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MpaRatingRepository mpaRatingRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Film testFilm1;
    private Film testFilm2;
    private User testUser;
    private MpaRating existingMpa1;
    private MpaRating existingMpa2;
    private Genre existingGenre;

    @BeforeEach
    void setUp() throws NotFoundException {
        // Очистка базы в правильном порядке (с учетом foreign key constraints)
        jdbcTemplate.update("DELETE FROM FILM_LIKE");
        jdbcTemplate.update("DELETE FROM FILM_GENRE");
        jdbcTemplate.update("DELETE FROM FRIENDSHIP");
        jdbcTemplate.update("DELETE FROM FILM");
        jdbcTemplate.update("DELETE FROM USERS");

        // Инициализация тестовых данных
        existingMpa1 = mpaRatingRepository.findByIdOrThrow(1L); // G
        existingMpa2 = mpaRatingRepository.findByIdOrThrow(2L); // PG
        existingGenre = genreRepository.findByIdOrThrow(1L); // Комедия

        testUser = User.builder()
                .name("Test User")
                .login("testlogin")
                .email("test@example.com")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        testFilm1 = Film.builder()
                .name("Test Film 1")
                .description("Test Description 1")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(120)
                .mpa(existingMpa1)
                .build();

        testFilm2 = Film.builder()
                .name("Test Film 2")
                .description("Test Description 2")
                .releaseDate(LocalDate.of(2021, 1, 1))
                .duration(90)
                .mpa(existingMpa2)
                .build();
    }

    @Test
    void shouldInsertAndFindFilm() throws ConditionsException, NotFoundException {
        Film createdFilm = filmRepository.insert(testFilm1);
        assertThat(createdFilm.getId()).isNotNull();

        Film foundFilm = filmRepository.findByIdOrThrow(createdFilm.getId());
        assertThat(foundFilm.getName()).isEqualTo("Test Film 1");
        assertThat(foundFilm.getMpa().getId()).isEqualTo(existingMpa1.getId());
    }

    @Test
    void shouldUpdateFilm() throws ConditionsException, NotFoundException {
        Film createdFilm = filmRepository.insert(testFilm1);
        Film updatedFilm = createdFilm.toBuilder()
                .name("Updated Film")
                .description("Updated Description")
                .genres(Set.of(existingGenre))
                .build();

        filmRepository.update(updatedFilm);
        Film filmAfterUpdate = filmRepository.findByIdOrThrow(updatedFilm.getId());

        assertThat(filmAfterUpdate.getName()).isEqualTo("Updated Film");
        assertThat(filmAfterUpdate.getDescription()).isEqualTo("Updated Description");
        assertThat(filmAfterUpdate.getGenres()).hasSize(1);
    }

    @Test
    void shouldDeleteFilm() throws ConditionsException, NotFoundException {
        Film createdFilm = filmRepository.insert(testFilm1);
        int deleteCount = filmRepository.deleteById(createdFilm.getId());

        assertThat(deleteCount).isEqualTo(1);
        assertThatThrownBy(() -> filmRepository.findByIdOrThrow(createdFilm.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldManageLikes() throws ConditionsException, NotFoundException {
        Film film = filmRepository.insert(testFilm1);
        User user = userRepository.insert(testUser);

        filmRepository.addLike(film.getId(), user.getId());
        assertThat(filmRepository.getFilmLikes(film.getId())).contains(user.getId());

        filmRepository.removeLike(film.getId(), user.getId());
        assertThat(filmRepository.getFilmLikes(film.getId())).isEmpty();
    }

    @Test
    void shouldFindAllFilms() throws ConditionsException, NotFoundException {
        filmRepository.insert(testFilm1);
        filmRepository.insert(testFilm2);

        List<Film> films = filmRepository.findAll();
        assertThat(films).hasSize(2);
    }

    @Test
    void shouldGetPopularFilms() throws ConditionsException, NotFoundException {
        User createdUser = userRepository.insert(testUser);
        Film createdFilm1 = filmRepository.insert(testFilm1);
        filmRepository.insert(testFilm2);

        // Добавляем лайк
        filmRepository.addLike(createdFilm1.getId(), createdUser.getId());

        List<Film> popularFilms = filmRepository.getPopularFilms(1L);
        assertThat(popularFilms)
                .hasSize(1)
                .extracting(Film::getId)
                .containsExactly(createdFilm1.getId());
    }
}