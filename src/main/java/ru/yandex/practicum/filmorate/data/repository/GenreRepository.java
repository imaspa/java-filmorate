package ru.yandex.practicum.filmorate.data.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.data.exception.NotFoundException;
import ru.yandex.practicum.filmorate.data.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class GenreRepository extends BaseRepository<Genre> {
    private static final String FIND_ALL_SQL = "SELECT * FROM GENRE ORDER BY ID";
    private static final String FIND_BY_ID_SQL = "SELECT * FROM GENRE WHERE ID = ?";
    private static final String EXISTS_BY_ID_SQL = "SELECT COUNT(*) > 0 FROM GENRE WHERE ID = ?";
    private static final String EXIST_ALL_BY_IDS_SQL = "SELECT COUNT(*) = ? FROM GENRE WHERE ID IN (%s)";

    private final JdbcTemplate jdbcTemplate;

    public GenreRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Genre> findAll() {
        return findAll(FIND_ALL_SQL, this::mapToGenre);
    }

    public Genre findByIdOrThrow(Long id) throws NotFoundException {
        return findByIdOrThrow(FIND_BY_ID_SQL, id, this::mapToGenre);
    }

    public boolean existsById(Long id) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(EXISTS_BY_ID_SQL, Boolean.class, id));
    }

    public boolean existAllByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return true;
        }
        String inClause = ids.stream().map(String::valueOf).collect(Collectors.joining(","));
        String sql = String.format(EXIST_ALL_BY_IDS_SQL, inClause);
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, ids.size()));
    }

    private Genre mapToGenre(ResultSet rs) throws SQLException {
        return new Genre(
                rs.getLong("ID"),
                rs.getString("NAME")
        );
    }
}