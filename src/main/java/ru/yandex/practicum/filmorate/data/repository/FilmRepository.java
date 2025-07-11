package ru.yandex.practicum.filmorate.data.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.data.exception.ConditionsException;
import ru.yandex.practicum.filmorate.data.exception.NotFoundException;
import ru.yandex.practicum.filmorate.data.model.Director;
import ru.yandex.practicum.filmorate.data.model.Film;
import ru.yandex.practicum.filmorate.data.model.Genre;
import ru.yandex.practicum.filmorate.data.model.MpaRating;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class FilmRepository extends BaseRepository<Film> {
    private static final String INSERT_SQL = "INSERT INTO FILM (NAME, DESCRIPTION, RELEASE_DATE, DURATION, MPA_ID) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_SQL = "UPDATE FILM SET NAME = ?, DESCRIPTION = ?, RELEASE_DATE = ?, DURATION = ?, MPA_ID = ? WHERE ID = ?";
    private static final String FIND_BY_ID_SQL = "SELECT f.*, m.NAME AS MPA_NAME FROM FILM f JOIN MPA_RATING m ON f.MPA_ID = m.ID WHERE f.ID = ?";
    private static final String FIND_ALL_SQL = """
            SELECT
                f.ID AS FILM_ID, f.NAME, f.DESCRIPTION, f.RELEASE_DATE, f.DURATION,
                   f.MPA_ID, m.NAME AS MPA_NAME,
                   g.ID AS GENRE_ID, g.NAME AS GENRE_NAME,
                   fl.USER_ID,
                   d.ID AS DIRECTOR_ID, d.NAME AS DIRECTOR_NAME
            FROM FILM f
            JOIN MPA_RATING m ON f.MPA_ID = m.ID
            LEFT JOIN FILM_GENRE fg ON f.ID = fg.FILM_ID
            LEFT JOIN GENRE g ON fg.GENRE_ID = g.ID
            LEFT JOIN FILM_LIKE fl ON f.ID = fl.FILM_ID
            LEFT JOIN FILM_DIRECTOR fd ON f.ID = fd.FILM_ID
            LEFT JOIN DIRECTOR d ON d.ID = fd.DIRECTOR_ID
            ORDER BY f.ID
            """;
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
    private static final String GET_POPULAR_FILMS_SQL = """
            SELECT
                f.ID AS FILM_ID, f.NAME, f.DESCRIPTION, f.RELEASE_DATE, f.DURATION,
                f.MPA_ID, m.NAME AS MPA_NAME,
                g.ID AS GENRE_ID, g.NAME AS GENRE_NAME,
                fl.USER_ID,
                COUNT(fl.USER_ID) OVER (PARTITION BY f.ID) AS LIKE_COUNT,
                d.ID AS DIRECTOR_ID, d.NAME AS DIRECTOR_NAME
            FROM FILM f
            JOIN MPA_RATING m ON f.MPA_ID = m.ID
            LEFT JOIN FILM_GENRE fg ON f.ID = fg.FILM_ID
            LEFT JOIN GENRE g ON fg.GENRE_ID = g.ID
            LEFT JOIN FILM_LIKE fl ON f.ID = fl.FILM_ID
            LEFT JOIN FILM_DIRECTOR fd ON f.ID = fd.FILM_ID
            LEFT JOIN DIRECTOR d ON d.ID = fd.DIRECTOR_ID
            ORDER BY LIKE_COUNT DESC
            LIMIT ?
            """;

    private static final String GET_FILMS_BY_DIRECTOR = """
            SELECT 
                f.ID AS FILM_ID, f.NAME, f.DESCRIPTION, f.RELEASE_DATE, f.DURATION,
                f.MPA_ID, m.NAME AS MPA_NAME,
                g.ID AS GENRE_ID, g.NAME AS GENRE_NAME,
                fl.USER_ID AS USER_ID,
                COUNT(fl.USER_ID) OVER (PARTITION BY f.ID) AS LIKES,
                EXTRACT(YEAR FROM f.RELEASE_DATE) AS YEARS,
                d.ID AS DIRECTOR_ID, d.NAME AS DIRECTOR_NAME
            FROM FILM AS f
                     JOIN MPA_RATING m ON f.MPA_ID = m.ID
                     LEFT JOIN FILM_GENRE fg ON f.ID = fg.FILM_ID
                     LEFT JOIN GENRE g ON fg.GENRE_ID = g.ID
                     LEFT JOIN FILM_LIKE fl ON f.ID = fl.FILM_ID
                     LEFT JOIN FILM_DIRECTOR fd ON f.ID = fd.FILM_ID
                     LEFT JOIN DIRECTOR d ON d.ID = fd.DIRECTOR_ID
            WHERE fd.DIRECTOR_ID = ?
            %s
            """;
    private static final String INSERT_DIRECTOR_SQL = "INSERT INTO FILM_DIRECTOR (FILM_ID, DIRECTOR_ID) VALUES (?, ?)";
    private static final String DELETE_DIRECTOR_SQL = "DELETE FROM FILM_DIRECTOR WHERE FILM_ID = ?";
    private static final String GET_DIRECTORS_SQL = "SELECT d.* FROM DIRECTOR AS d JOIN FILM_DIRECTOR AS fd ON d.ID = fd.DIRECTOR_ID WHERE fd.FILM_ID = ?";

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
        updateFilmGenres(insertedFilm.getId(), film.getGenres());
        updateFilmDirectors(insertedFilm.getId(), film.getDirectors());
        return insertedFilm;
    }

    private void updateFilmDirectors(Long filmId, Set<Director> directors) {
        if (directors == null || directors.isEmpty()) {
            return;
        }
        jdbcTemplate.batchUpdate(INSERT_DIRECTOR_SQL, directors.stream()
                .map(director -> new Object[]{filmId, director.getId()})
                .collect(Collectors.toList()));
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
        updateFilmGenres(film.getId(), film.getGenres());
        jdbcTemplate.update(DELETE_DIRECTOR_SQL, film.getId());
        updateFilmDirectors(film.getId(), film.getDirectors());
        return updatedFilm;
    }

    public Optional<Film> findById(Long id) {
        Optional<Film> film = findById(FIND_BY_ID_SQL, id, this::mapToFilm);
        film.ifPresent(f -> {
            f.setGenres(getFilmGenres(f.getId()));
            f.setLikes(getFilmLikes(f.getId()));
            f.setDirectors(getFilmDirectors(f.getId()));
        });
        return film;
    }

    public int deleteById(Long id) {
        return deleteById(DELETE_SQL, id);
    }

    public Film findByIdOrThrow(Long id) throws NotFoundException {
        Film film = findByIdOrThrow(FIND_BY_ID_SQL, id, this::mapToFilm);
        film.setGenres(getFilmGenres(film.getId()));
        film.setLikes(getFilmLikes(film.getId()));
        film.setDirectors(getFilmDirectors(film.getId()));
        return film;
    }

    private Set<Director> getFilmDirectors(Long filmId) {
        return jdbcTemplate.query(GET_DIRECTORS_SQL, (rs, rowNum) -> mapToDirector(rs), filmId)
                .stream()
                .collect(Collectors.toSet());
    }

    private Director mapToDirector(ResultSet rs) throws SQLException {
        return new Director(
                rs.getLong("ID"),
                rs.getString("NAME"));
    }

    private void updateFilmGenres(Long filmId, Set<Genre> genres) {
        if (genres == null || genres.isEmpty()) {
            return;
        }
        jdbcTemplate.batchUpdate(INSERT_GENRE_SQL, genres.stream()
                .map(genre -> new Object[]{filmId, genre.getId()})
                .collect(Collectors.toList()));
    }

    private Set<Genre> getFilmGenres(Long filmId) {
        return jdbcTemplate.query(GET_GENRES_SQL, (rs, rowNum) -> mapToGenre(rs), filmId)
                .stream()
                .collect(Collectors.toSet());
    }

    public void addLike(Long filmId, Long userId) throws ConditionsException {
        if (isLikeExists(filmId, userId)) {
            throw new ConditionsException("Лайк уже учтен");
        }
        jdbcTemplate.update(ADD_LIKE_SQL, filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) throws ConditionsException {
        if (!isLikeExists(filmId, userId)) {
            throw new ConditionsException("Пользователь не может удалить лайк, который не добавлял");
        }
        jdbcTemplate.update(REMOVE_LIKE_SQL, filmId, userId);
    }

    public Boolean isLikeExists(Long filmId, Long userId) {
        return jdbcTemplate.queryForObject(CHECK_LIKE_EXISTS_SQL, Boolean.class, filmId, userId);
    }

    public Set<Long> getFilmLikes(Long filmId) {
        return jdbcTemplate.queryForList(GET_LIKES_SQL, Long.class, filmId)
                .stream()
                .collect(Collectors.toSet());
    }

    public List<Film> getPopularFilms(Long limit) {
        return executeFilmQuery(GET_POPULAR_FILMS_SQL, limit);
    }

    public List<Film> findAll() {
        return executeFilmQuery(FIND_ALL_SQL);
    }

    private List<Film> executeFilmQuery(String sql, Object... args) {
        return jdbcTemplate.query(sql, this::mapFilmResultSet, args);
    }

    private Film mapFilmResultSet(ResultSet rs, int rowNum) throws SQLException {
        Long filmId = rs.getLong("FILM_ID");

        Film film = Film.builder()
                .id(filmId)
                .name(rs.getString("NAME"))
                .description(rs.getString("DESCRIPTION"))
                .releaseDate(rs.getDate("RELEASE_DATE").toLocalDate())
                .duration(rs.getInt("DURATION"))
                .mpa(new MpaRating(rs.getLong("MPA_ID"), rs.getString("MPA_NAME")))
                .genres(new HashSet<>())
                .likes(new HashSet<>())
                .directors(new HashSet<>())
                .build();
        Long genreId = rs.getLong("GENRE_ID");
        if (!rs.wasNull()) {
            film.getGenres().add(new Genre(
                    genreId,
                    rs.getString("GENRE_NAME")));
        }
        Long userId = rs.getLong("USER_ID");
        if (!rs.wasNull()) {
            film.getLikes().add(userId);
        }
        Long directorId = rs.getLong("DIRECTOR_ID");
        if (!rs.wasNull()) {
            film.getDirectors().add(new Director(
                    directorId,
                    rs.getString("DIRECTOR_NAME")));
        }
        return film;
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

    public List<Film> getFilmByDirector(Long directorId, String sortBy) {
        String sql = GET_FILMS_BY_DIRECTOR;
        String orderby = "ID";
        if (sortBy != null) {
            if (sortBy.equals("year")) {
                orderby = "YEARS";
            }
            if (sortBy.equals("likes")) {
                orderby = "LIKES DESC";
            }
        }
        sql = String.format(sql, String.format(" ORDER BY %s", orderby));
        return executeFilmQuery(sql, directorId).stream().distinct().toList();
    }
}