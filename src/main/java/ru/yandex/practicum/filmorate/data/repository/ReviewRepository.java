package ru.yandex.practicum.filmorate.data.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.data.exception.ConditionsException;
import ru.yandex.practicum.filmorate.data.exception.NotFoundException;
import ru.yandex.practicum.filmorate.data.model.Review;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class ReviewRepository extends BaseRepository<Review> {
    private final JdbcTemplate jdbcTemplate;
    private static final String INSERT_SQL = "INSERT INTO REVIEW (user_id, film_id, content, is_positive, useful) VALUES (?,?,?,?,?)";
    private static final String UPDATE_SQL = "UPDATE REVIEW SET user_id = ?,film_id = ?,content = ?,is_positive = ? WHERE id=?";
    private static final String FIND_BY_ID_SQL = "SELECT * FROM REVIEW WHERE id = ?";
    private static final String DELETE_REVIEW_BY_ID = "DELETE FROM REVIEW WHERE id = ?";
    private static final String FIND_ALL = "SELECT * FROM REVIEW LIMIT ?";
    private static final String FIND_ALL_BY_FILM = "SELECT * FROM REVIEW WHERE film_id = ? LIMIT ?";

    public ReviewRepository(JdbcTemplate jdbcTemplate, JdbcTemplate jdbcTemplate1) {
        super(jdbcTemplate);
        this.jdbcTemplate = jdbcTemplate1;
    }

    public Review insert(Review review) throws ConditionsException {
        Review savedReview = insert(INSERT_SQL, (ps, f) -> {
            ps.setLong(1, f.getUserId());
            ps.setLong(2, f.getFilmId());
            ps.setString(3, f.getContent());
            ps.setBoolean(4, f.getIsPositive());
            ps.setInt(5, 0);
        }, review);
        return savedReview;
    }

    public Review update(Review review) throws ConditionsException {
        Review savedReview = update(UPDATE_SQL, (ps, f) -> {
            ps.setLong(1, f.getUserId());
            ps.setLong(2, f.getFilmId());
            ps.setString(3, f.getContent());
            ps.setBoolean(4, f.getIsPositive());
            ps.setLong(5, f.getId());
        }, review);
        return savedReview;
    }

    public Review findByIdOrThrow(Long id) throws NotFoundException {
        Review review = findByIdOrThrow(FIND_BY_ID_SQL, id, this::mapToReview);
        return review;
    }

    private Review mapToReview(ResultSet rs) throws SQLException {
        return Review.builder()
                .id(rs.getLong("ID"))
                .userId(rs.getLong("USER_ID"))
                .filmId(rs.getLong("FILM_ID"))
                .content(rs.getString("CONTENT"))
                .isPositive(rs.getBoolean("IS_POSITIVE"))
                .useful(rs.getInt("USEFUL"))
                .build();
    }

    private Review mapResultSetToReview(ResultSet rs, int rowNum) throws SQLException {
        return mapToReview(rs);
    }

    public int deleteById(Long id) {
        return deleteById(DELETE_REVIEW_BY_ID, id);
    }

    public List<Review> getAll(Long filmId, Long count) {
        if (filmId != null) {
            return jdbcTemplate.query(FIND_ALL_BY_FILM, this::mapResultSetToReview, filmId, count);
        }
        return jdbcTemplate.query(FIND_ALL, this::mapResultSetToReview, count);
    }
}
