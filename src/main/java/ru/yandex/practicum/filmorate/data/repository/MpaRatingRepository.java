package ru.yandex.practicum.filmorate.data.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.data.exception.NotFoundException;
import ru.yandex.practicum.filmorate.data.model.MpaRating;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class MpaRatingRepository extends BaseRepository<MpaRating> {
    private static final String FIND_ALL_SQL = "SELECT * FROM MPA_RATING ORDER BY ID";
    private static final String FIND_BY_ID_SQL = "SELECT * FROM MPA_RATING WHERE ID = ?";
    private static final String EXISTS_BY_ID_SQL = "SELECT COUNT(*) > 0 FROM MPA_RATING WHERE ID = ?";

    private final JdbcTemplate jdbcTemplate;

    public MpaRatingRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<MpaRating> findAll() {
        return findAll(FIND_ALL_SQL, this::mapToMpaRating);
    }

    public MpaRating findByIdOrThrow(Long id) throws NotFoundException {
        return findByIdOrThrow(FIND_BY_ID_SQL, id, this::mapToMpaRating);
    }

    public boolean existsById(Long id) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(EXISTS_BY_ID_SQL, Boolean.class, id));
    }

    private MpaRating mapToMpaRating(ResultSet rs) throws SQLException {
        return new MpaRating(
                rs.getLong("ID"),
                rs.getString("NAME")
        );
    }
}