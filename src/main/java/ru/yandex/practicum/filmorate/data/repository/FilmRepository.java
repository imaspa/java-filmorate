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
import java.util.ArrayList;
import java.util.Collections;
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
    private static final String GET_USERS_WITH_SAME_LIKES_SQL = """
            SELECT fl.user_id, COUNT(fl.film_id) AS rate
            FROM film_like ul
            JOIN film_like fl ON ul.film_id = fl.film_id
            JOIN users u ON (fl.user_id != u.id)
            WHERE ul.user_id = ? AND ul.user_id != fl.user_id
            GROUP BY fl.user_id
            HAVING rate > 1
            ORDER BY rate DESC
            LIMIT ?
            """;
    private static final String SEARCH_FILMS_SQL = """
            SELECT
                f.ID AS FILM_ID, f.NAME, f.DESCRIPTION, f.RELEASE_DATE, f.DURATION,
                f.MPA_ID, m.NAME AS MPA_NAME,
                g.ID AS GENRE_ID, g.NAME AS GENRE_NAME,
                fl.USER_ID,
                d.ID AS DIRECTOR_ID, d.NAME AS DIRECTOR_NAME,
                (SELECT COUNT(*) FROM FILM_LIKE fl2 WHERE fl2.FILM_ID = f.ID) AS LIKE_COUNT
            FROM FILM f
            JOIN MPA_RATING m ON f.MPA_ID = m.ID
            LEFT JOIN FILM_GENRE fg ON f.ID = fg.FILM_ID
            LEFT JOIN GENRE g ON fg.GENRE_ID = g.ID
            LEFT JOIN FILM_LIKE fl ON f.ID = fl.FILM_ID
            LEFT JOIN FILM_DIRECTOR fd ON f.ID = fd.FILM_ID
            LEFT JOIN DIRECTOR d ON fd.DIRECTOR_ID = d.ID
            """;
    private static String GET_RECOMMENDATIONS_SQL = """
            SELECT fl.film_id
            FROM film_like fl
            WHERE fl.user_id IN (%s)
              AND fl.film_id NOT IN (
                SELECT ul.film_id
                FROM film_like ul
                WHERE ul.user_id = ?
                )
            """;

    private static String GET_COMMON_FILMS = """
            SELECT
                f.ID                         AS FILM_ID,
                f.NAME                       AS FILM_NAME,
                f.DESCRIPTION                AS DESCRIPTION,
                f.RELEASE_DATE               AS RELEASE_DATE,
                f.DURATION                   AS DURATION,
                f.MPA_ID                     AS MPA_ID,
                m.NAME                       AS MPA_NAME,
                g.ID                         AS GENRE_ID,
                g.NAME                       AS GENRE_NAME,
                d.ID                         AS DIRECTOR_ID,
                d.NAME                       AS DIRECTOR_NAME,
                fl.USER_ID                   AS LIKE_USER_ID,
                -- Подсчёт общего количества лайков по фильму (рейтинг)
                (SELECT COUNT(*) FROM FILM_LIKE fl2 WHERE fl2.FILM_ID = f.ID) AS LIKE_COUNT
            FROM FILM f
            -- Ограничиваем фильмы, которые полайкали оба пользователя
            JOIN FILM_LIKE fl1 ON f.ID = fl1.FILM_ID
            JOIN FILM_LIKE fl2 ON f.ID = fl2.FILM_ID AND fl1.USER_ID <> fl2.USER_ID
            -- Присоединяем другие атрибуты
            LEFT JOIN MPA_RATING m ON f.MPA_ID = m.ID
            LEFT JOIN FILM_GENRE fg ON f.ID = fg.FILM_ID
            LEFT JOIN GENRE g ON fg.GENRE_ID = g.ID
            LEFT JOIN FILM_DIRECTOR fd ON f.ID = fd.FILM_ID
            LEFT JOIN DIRECTOR d ON fd.DIRECTOR_ID = d.ID
            LEFT JOIN FILM_LIKE fl ON f.ID = fl.FILM_ID -- нужен для LIKE_USER_ID
            WHERE fl1.USER_ID = ? AND fl2.USER_ID = ?
            ORDER BY f.ID, g.ID, d.ID;
            """;

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

    public Film findByIdOrThrow(Long id) throws NotFoundException {
        Film film = findByIdOrThrow(FIND_BY_ID_SQL, id, this::mapToFilm);
        film.setGenres(getFilmGenres(film.getId()));
        film.setLikes(getFilmLikes(film.getId()));
        film.setDirectors(getFilmDirectors(film.getId()));
        return film;
    }

    public int deleteById(Long id) {
        return deleteById(DELETE_SQL, id);
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

    public List<Film> getPopularFilms(Long count, Integer year, Long genreId) {
        String newsql = "";
        List<Object> params = new ArrayList<>();
        if (year != null || genreId != null) {
            newsql += " WHERE ";
            if (year != null) {
                newsql += "YEAR(f.RELEASE_DATE) = ? ";
                params.add(year);
                if (genreId != null) {
                    newsql += "AND ";
                }
            }
            if (genreId != null) {
                newsql += "fg.GENRE_ID = ? ";
                params.add(genreId);
            }
        }
        String sql = GET_POPULAR_FILMS_SQL + newsql +
                """
                        ORDER BY LIKE_COUNT DESC
                        LIMIT ?
                        """;
        params.add(count);
        return jdbcTemplate.query(sql, this::mapFilmResultSet, params.toArray());
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

    public List<Film> getCommonFilms(Long userId, Long friendId) {
        return executeFilmQuery(GET_COMMON_FILMS, userId, friendId).stream().distinct().toList();
    }

    public List<Long> getUsersWithSameLikes(Long userId, Integer limit) {
        return jdbcTemplate.query(GET_USERS_WITH_SAME_LIKES_SQL, (rs, rowNum) -> rs.getLong("user_id"), userId, limit);
    }

    public List<Long> getFilmRecommendations(Long userId, List<Long> sameUserIds) {
        if (sameUserIds.isEmpty()) {
            return Collections.emptyList();
        }
        String placeholders = String.join(", ", Collections.nCopies(sameUserIds.size(), "?"));
        GET_RECOMMENDATIONS_SQL = String.format(GET_RECOMMENDATIONS_SQL, placeholders);
        Object[] args = new Object[sameUserIds.size() + 1];
        System.arraycopy(sameUserIds.toArray(), 0, args, 0, sameUserIds.size());
        args[sameUserIds.size()] = userId;
        return jdbcTemplate.queryForList(GET_RECOMMENDATIONS_SQL, args, Long.class);
    }

    public List<Film> searchFilms(String query, String[] searchBy) {
        if (query == null || query.isBlank() || searchBy == null || searchBy.length == 0) {
            return Collections.emptyList();
        }

        boolean searchByTitle = false;
        boolean searchByDirector = false;

        for (String param : searchBy) {
            if ("title".equalsIgnoreCase(param.trim())) searchByTitle = true;
            if ("director".equalsIgnoreCase(param.trim())) searchByDirector = true;
        }

        if (!searchByTitle && !searchByDirector) {
            return Collections.emptyList();
        }

        String sql = buildSearchQuery(searchByTitle, searchByDirector);
        String searchPattern = "%" + query.toLowerCase() + "%";

        if (searchByTitle && searchByDirector) {
            return executeFilmQuery(sql, searchPattern, searchPattern);
        } else {
            return executeFilmQuery(sql, searchPattern);
        }
    }

    private String buildSearchQuery(boolean searchByTitle, boolean searchByDirector) {
        StringBuilder sql = new StringBuilder(SEARCH_FILMS_SQL);

        List<String> conditions = new ArrayList<>();
        if (searchByTitle) {
            conditions.add("LOWER(f.NAME) LIKE LOWER(?)");
        }
        if (searchByDirector) {
            conditions.add("LOWER(d.NAME) LIKE LOWER(?)");
        }

        if (!conditions.isEmpty()) {
            sql.append("WHERE ");
            sql.append(String.join(" OR ", conditions));
        }

        sql.append(" ORDER BY LIKE_COUNT DESC");

        return sql.toString();
    }
}