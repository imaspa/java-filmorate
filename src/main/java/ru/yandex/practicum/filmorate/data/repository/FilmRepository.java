package ru.yandex.practicum.filmorate.data.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.data.exception.ConditionsException;
import ru.yandex.practicum.filmorate.data.exception.NotFoundException;
import ru.yandex.practicum.filmorate.data.model.Film;
import ru.yandex.practicum.filmorate.data.model.Genre;
import ru.yandex.practicum.filmorate.data.model.MpaRating;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class FilmRepository extends BaseRepository<Film> {
    private static final String INSERT_SQL = "INSERT INTO FILM (NAME, DESCRIPTION, RELEASE_DATE, DURATION, MPA_ID) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_SQL = "UPDATE FILM SET NAME = ?, DESCRIPTION = ?, RELEASE_DATE = ?, DURATION = ?, MPA_ID = ? WHERE ID = ?";
    private static final String FIND_BY_ID_SQL = "SELECT f.*, m.NAME AS MPA_NAME FROM FILM f JOIN MPA_RATING m ON f.MPA_ID = m.ID WHERE f.ID = ?";
    private static final String FIND_ALL_SQL = "SELECT f.*, m.NAME AS MPA_NAME FROM FILM f JOIN MPA_RATING m ON f.MPA_ID = m.ID";
    private static final String DELETE_SQL = "DELETE FROM FILM WHERE ID = ?";

    // Жанры
    private static final String INSERT_GENRE_SQL = "INSERT INTO FILM_GENRE (FILM_ID, GENRE_ID) VALUES (?, ?)";
    private static final String DELETE_GENRES_SQL = "DELETE FROM FILM_GENRE WHERE FILM_ID = ?";
    private static final String GET_GENRES_SQL = "SELECT g.* FROM GENRE g JOIN FILM_GENRE fg ON g.ID = fg.GENRE_ID WHERE fg.FILM_ID = ?";

    // Лайки
    private static final String CHECK_LIKE_EXISTS_SQL = "SELECT COUNT(*) > 0 FROM FILM_LIKE WHERE FILM_ID = ? AND USER_ID = ?";
    private static final String ADD_LIKE_SQL = "INSERT INTO FILM_LIKE (FILM_ID, USER_ID) VALUES (?, ?)";
    private static final String REMOVE_LIKE_SQL = "DELETE FROM FILM_LIKE WHERE FILM_ID = ? AND USER_ID = ?";
    private static final String GET_LIKES_SQL = "SELECT USER_ID FROM FILM_LIKE WHERE FILM_ID = ?";
    private static final String GET_POPULAR_FILMS_SQL = "SELECT f.*, m.NAME AS MPA_NAME FROM FILM f " +
            "JOIN MPA_RATING m ON f.MPA_ID = m.ID " +
            "LEFT JOIN FILM_LIKE fl ON f.ID = fl.FILM_ID " +
            "GROUP BY f.ID " +
            "ORDER BY COUNT(fl.USER_ID) DESC " +
            "LIMIT ?";

    private final JdbcTemplate jdbcTemplate;

    public FilmRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
        this.jdbcTemplate = jdbcTemplate;
    }

    public Film insert(Film film) throws ConditionsException {
        Film insertedFilm = insert(INSERT_SQL, (ps, f) -> {
            ps.setString(1, f.getName());
            ps.setString(2, f.getDescription());
            ps.setDate(3, Date.valueOf(f.getReleaseDate()));
            ps.setInt(4, f.getDuration());
            ps.setLong(5, f.getMpa().getId());
        }, film);

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            updateFilmGenres(insertedFilm.getId(), film.getGenres());
        }

        return insertedFilm;
    }

    public Film update(Film film) throws ConditionsException {
        Film updatedFilm = update(UPDATE_SQL, (ps, f) -> {
            ps.setString(1, f.getName());
            ps.setString(2, f.getDescription());
            ps.setDate(3, Date.valueOf(f.getReleaseDate()));
            ps.setInt(4, f.getDuration());
            ps.setLong(5, f.getMpa().getId());
            ps.setLong(6, f.getId());
        }, film);

        jdbcTemplate.update(DELETE_GENRES_SQL, film.getId());
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            updateFilmGenres(film.getId(), film.getGenres());
        }

        return updatedFilm;
    }

    public Optional<Film> findById(Long id) {
        Optional<Film> film = findById(FIND_BY_ID_SQL, id, this::mapToFilm);
        film.ifPresent(f -> {
            f.setGenres(getFilmGenres(f.getId()));
            f.setLikes(getFilmLikes(f.getId()));
        });
        return film;
    }

    public List<Film> findAll() {
        List<Film> films = findAll(FIND_ALL_SQL, this::mapToFilm);
        films.forEach(f -> {
            f.setGenres(getFilmGenres(f.getId()));
            f.setLikes(getFilmLikes(f.getId()));
        });
        return films;
    }

    public int deleteById(Long id) {
        return deleteById(DELETE_SQL, id);
    }

    public Film findByIdOrThrow(Long id) throws NotFoundException {
        Film film = findByIdOrThrow(FIND_BY_ID_SQL, id, this::mapToFilm);
        film.setGenres(getFilmGenres(film.getId()));
        film.setLikes(getFilmLikes(film.getId()));
        return film;
    }

    private void updateFilmGenres(Long filmId, Set<Genre> genres) {
        genres.forEach(genre -> jdbcTemplate.update(INSERT_GENRE_SQL, filmId, genre.getId()));
    }

    private Set<Genre> getFilmGenres(Long filmId) {
        return jdbcTemplate.query(GET_GENRES_SQL, (rs, rowNum) -> mapToGenre(rs), filmId)
                .stream()
                .collect(Collectors.toSet());
    }

    public void addLike(Long filmId, Long userId) throws ConditionsException {
        Boolean alreadyLiked = jdbcTemplate.queryForObject(CHECK_LIKE_EXISTS_SQL, Boolean.class, filmId, userId);
        if (Boolean.TRUE.equals(alreadyLiked)) {
            throw new ConditionsException("Лайк уже учтен");
        }
        jdbcTemplate.update(ADD_LIKE_SQL, filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) throws ConditionsException {
        Boolean likeExists = jdbcTemplate.queryForObject(CHECK_LIKE_EXISTS_SQL, Boolean.class, filmId, userId);
        if (Boolean.FALSE.equals(likeExists)) {
            throw new ConditionsException("Пользователь не может удалить лайк, который не добавлял");
        }
        jdbcTemplate.update(REMOVE_LIKE_SQL, filmId, userId);
    }

    public Set<Long> getFilmLikes(Long filmId) {
        return jdbcTemplate.queryForList(GET_LIKES_SQL, Long.class, filmId)
                .stream()
                .collect(Collectors.toSet());
    }

    public List<Film> getPopularFilms(Long count) {
        List<Film> films = jdbcTemplate.query(GET_POPULAR_FILMS_SQL, (rs, rowNum) -> mapToFilm(rs), count);
        films.forEach(f -> {
            f.setGenres(getFilmGenres(f.getId()));
            f.setLikes(getFilmLikes(f.getId()));
        });
        return films;
    }

    private Film mapToFilm(ResultSet rs) throws SQLException {
        return Film.builder()
                .id(rs.getLong("ID"))
                .name(rs.getString("NAME"))
                .description(rs.getString("DESCRIPTION"))
                .releaseDate(rs.getDate("RELEASE_DATE").toLocalDate())
                .duration(rs.getInt("DURATION"))
                .mpa(new MpaRating(
                        rs.getLong("MPA_ID"),
                        rs.getString("MPA_NAME")
                ))
                .build();
    }

    private Genre mapToGenre(ResultSet rs) throws SQLException {
        return new Genre(
                rs.getLong("ID"),
                rs.getString("NAME"));
    }
}