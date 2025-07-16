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
import java.util.Arrays;
import java.util.Collections;
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
    public void shouldReturnRecommendationsForUserWithMultipleLikes() {
        // Создаем пользователей
        jdbcTemplate.update("INSERT INTO USERS (ID, NAME, LOGIN, EMAIL, BIRTHDAY) VALUES (?, ?, ?, ?, ?)",
                1L, "User1", "user1", "user1@test.com", "1990-01-01");
        jdbcTemplate.update("INSERT INTO USERS (ID, NAME, LOGIN, EMAIL, BIRTHDAY) VALUES (?, ?, ?, ?, ?)",
                2L, "User2", "user2", "user2@test.com", "1990-02-02");
        jdbcTemplate.update("INSERT INTO USERS (ID, NAME, LOGIN, EMAIL, BIRTHDAY) VALUES (?, ?, ?, ?, ?)",
                3L, "User3", "user3", "user3@test.com", "1990-03-03");

        // Создаем фильмы
        jdbcTemplate.update("INSERT INTO FILM (ID, NAME, DESCRIPTION, RELEASE_DATE, DURATION, MPA_ID) VALUES (?, ?, ?, ?, ?, ?)",
                100L, "Film 1", "Description 1", "2000-01-01", 120, 1);
        jdbcTemplate.update("INSERT INTO FILM (ID, NAME, DESCRIPTION, RELEASE_DATE, DURATION, MPA_ID) VALUES (?, ?, ?, ?, ?, ?)",
                101L, "Film 2", "Description 2", "2001-01-01", 90, 2);
        jdbcTemplate.update("INSERT INTO FILM (ID, NAME, DESCRIPTION, RELEASE_DATE, DURATION, MPA_ID) VALUES (?, ?, ?, ?, ?, ?)",
                102L, "Film 3", "Description 3", "2002-01-01", 110, 3);
        jdbcTemplate.update("INSERT INTO FILM (ID, NAME, DESCRIPTION, RELEASE_DATE, DURATION, MPA_ID) VALUES (?, ?, ?, ?, ?, ?)",
                103L, "Film 4", "Description 4", "2003-01-01", 100, 2);
        jdbcTemplate.update("INSERT INTO FILM (ID, NAME, DESCRIPTION, RELEASE_DATE, DURATION, MPA_ID) VALUES (?, ?, ?, ?, ?, ?)",
                104L, "Film 5", "Description 5", "2004-01-01", 130, 3);

        // Создаем лайки
        jdbcTemplate.update("INSERT INTO FILM_LIKE (FILM_ID ,USER_ID) VALUES (?, ?)", 100L ,1L);
        jdbcTemplate.update("INSERT INTO FILM_LIKE (FILM_ID ,USER_ID) VALUES (?, ?)", 101L ,1L);
        jdbcTemplate.update("INSERT INTO FILM_LIKE (FILM_ID ,USER_ID) VALUES (?, ?)", 102L ,1L);

        jdbcTemplate.update("INSERT INTO FILM_LIKE (FILM_ID ,USER_ID) VALUES (?, ?)", 103L ,2L);
        jdbcTemplate.update("INSERT INTO FILM_LIKE (FILM_ID ,USER_ID) VALUES (?, ?)", 104L ,2L);

        jdbcTemplate.update("INSERT INTO FILM_LIKE (FILM_ID ,USER_ID) VALUES (?, ?)", 101L ,3L);
        jdbcTemplate.update("INSERT INTO FILM_LIKE (FILM_ID ,USER_ID) VALUES (?, ?)", 102L ,3L);

        // Предполагаемый список похожих пользователей
        List<Long> sameUserIds = Arrays.asList(2L, 3L);

        // Получение рекомендаций для пользователя с id=1
        List<Long> recommendations = filmRepository.getFilmRecommendations(1L ,sameUserIds);

        // Проверка: рекомендации не должны включать уже просмотренные фильмы пользователем с ID=1
        assertThat(recommendations).isNotNull();
        assertThat(recommendations).containsExactlyInAnyOrder(103L, 104L); // например
    }

    @Test
    public void shouldReturnEmptyWhenNoRecommendations() {
        // Создаем пользователей
        jdbcTemplate.update("INSERT INTO USERS (ID, NAME, LOGIN, EMAIL, BIRTHDAY) VALUES (?, ?, ?, ?, ?)",
                1L, "User1", "user1", "user1@test.com", "1990-01-01");

        // Создаем фильмы без лайков или с лайками только текущего пользователя
        jdbcTemplate.update("INSERT INTO FILM (ID, NAME, DESCRIPTION, RELEASE_DATE, DURATION, MPA_ID) VALUES (?, ?, ?, ?, ?, ?)",
                100L, "Фильм 1", "Описание 1", "2000-01-01", 120, 1);

        // Нет лайков от других пользователей
        List<Long> sameUserIds = Arrays.asList();
        List<Long> recommendations = filmRepository.getFilmRecommendations(10L, sameUserIds);
        System.out.println(recommendations.stream().toList());
        assertThat(recommendations).isEmpty();
    }

    @Test
    void shouldGetPopularFilms() throws ConditionsException, NotFoundException {
        User createdUser = userRepository.insert(testUser);
        Film createdFilm1 = filmRepository.insert(testFilm1);
        filmRepository.insert(testFilm2);

        filmRepository.addLike(createdFilm1.getId(), createdUser.getId());

        List<Film> popularFilms = filmRepository.getPopularFilms(1L, null, null);
        assertThat(popularFilms)
                .hasSize(1)
                .extracting(Film::getId)
                .containsExactly(createdFilm1.getId());
    }

    @Test
    void shouldGetPopularFilmsByYears() throws ConditionsException, NotFoundException {
        User createdUser = userRepository.insert(testUser);
        Film createdFilm1 = filmRepository.insert(testFilm1);
        filmRepository.insert(testFilm2);

        filmRepository.addLike(createdFilm1.getId(), createdUser.getId());

        List<Film> popularFilms = filmRepository.getPopularFilms(1L, 2020, null);
        assertThat(popularFilms)
                .hasSize(1)
                .extracting(Film::getId)
                .containsExactly(createdFilm1.getId());
    }

    @Test
    void shouldGetPopularFilmsByGenre() throws ConditionsException, NotFoundException {
        testFilm1.setGenres(Collections.singleton(existingGenre));
        User createdUser = userRepository.insert(testUser);
        Film createdFilm1 = filmRepository.insert(testFilm1);
        filmRepository.insert(testFilm2);

        filmRepository.addLike(createdFilm1.getId(), createdUser.getId());

        List<Film> popularFilms = filmRepository.getPopularFilms(1L, null,1L);
        assertThat(popularFilms)
                .hasSize(1)
                .extracting(Film::getId)
                .containsExactly(createdFilm1.getId());
    }

    @Test
    void shouldGetPopularFilmsByYearsAndGenre() throws ConditionsException, NotFoundException {
        testFilm1.setGenres(Collections.singleton(existingGenre));
        User createdUser = userRepository.insert(testUser);
        Film createdFilm1 = filmRepository.insert(testFilm1);
        filmRepository.insert(testFilm2);

        filmRepository.addLike(createdFilm1.getId(), createdUser.getId());

        List<Film> popularFilms = filmRepository.getPopularFilms(1L, 2020, 1L);
        assertThat(popularFilms)
                .hasSize(1)
                .extracting(Film::getId)
                .containsExactly(createdFilm1.getId());
    }
}