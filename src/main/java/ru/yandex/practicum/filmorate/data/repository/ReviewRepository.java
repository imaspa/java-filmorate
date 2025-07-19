package ru.yandex.practicum.filmorate.data.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.data.exception.ConditionsException;
import ru.yandex.practicum.filmorate.data.exception.NotFoundException;
import ru.yandex.practicum.filmorate.data.model.Review;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class ReviewRepository extends BaseRepository<Review> {
    private static final String INSERT_SQL = "INSERT INTO REVIEW (user_id, film_id, content, is_positive, useful) VALUES (?,?,?,?,?)";
    private static final String UPDATE_SQL = "UPDATE REVIEW SET user_id = ?,film_id = ?,content = ?,is_positive = ? WHERE id=?";
    private static final String FIND_BY_ID_SQL = "SELECT * FROM REVIEW WHERE id = ?";
    private static final String EXISTS_BY_ID_SQL = "SELECT EXISTS(SELECT 1 FROM REVIEW WHERE ID = ?)";
    private static final String DELETE_REVIEW_BY_ID = "DELETE FROM REVIEW WHERE id = ?";
    private static final String FIND_ALL = "SELECT * FROM REVIEW ORDER BY USEFUL DESC LIMIT ?";
    private static final String FIND_ALL_BY_FILM = "SELECT * FROM REVIEW WHERE film_id = ? ORDER BY USEFUL DESC LIMIT ?";
    private static final String REVIEW_USEFUL_UP = "UPDATE REVIEW SET useful = useful + ? WHERE id = ?";
    private static final String REVIEW_USEFUL_DOWN = "UPDATE REVIEW SET useful = useful - ? WHERE id = ?";
    private static final String LIKE = "MERGE INTO REVIEW_LIKE(review_id, user_id, is_dislike) VALUES (?,?,?)";
    private static final String DELETE_LIKE = "DELETE FROM REVIEW_LIKE WHERE REVIEW_ID = ? AND USER_ID = ?";
    private static final String SELECT_LIKE = "SELECT is_dislike FROM REVIEW_LIKE WHERE REVIEW_ID = ? AND USER_ID = ? LIMIT 1";
    private final JdbcTemplate jdbcTemplate;

    public ReviewRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
        this.jdbcTemplate = jdbcTemplate;
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

    public Optional<Review> findById(Long id) throws NotFoundException {
        return findById(FIND_BY_ID_SQL, id, this::mapToReview);
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

    public int deleteById(Long id) throws NotFoundException {
        findByIdOrThrow(id);
        return deleteById(DELETE_REVIEW_BY_ID, id);
    }

    public List<Review> getAll(Long filmId, Long count) {
        if (filmId != null) {
            return jdbcTemplate.query(FIND_ALL_BY_FILM, this::mapResultSetToReview, filmId, count);
        }
        return jdbcTemplate.query(FIND_ALL, this::mapResultSetToReview, count);
    }

    public void addLike(Long id, Long userId, boolean isDislike) {
        Boolean existingReaction = getIsDislike(id, userId);
        int count = jdbcTemplate.update(LIKE, id, userId, isDislike);
        if (existingReaction != null) {
            if (existingReaction == isDislike) {
                return;
            }
            count++;
        }
        String updateQuery = isDislike ? REVIEW_USEFUL_DOWN : REVIEW_USEFUL_UP;
        jdbcTemplate.update(updateQuery, count, id);
    }

    public void deleteLike(Long id, Long userid, boolean isDislike) {
        int count = jdbcTemplate.update(DELETE_LIKE, id, userid);
        if (count == 0) return;

        String updateQuery = isDislike ? REVIEW_USEFUL_UP : REVIEW_USEFUL_DOWN;
        jdbcTemplate.update(updateQuery, count, id);
    }

    public Boolean getIsDislike(Long id, Long userId) {
        try {
            return jdbcTemplate.queryForObject(SELECT_LIKE, Boolean.class, id, userId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public Boolean existsById(Long id) {
        return jdbcTemplate.queryForObject(EXISTS_BY_ID_SQL, Boolean.class, id);
    }

    public void checkExists(Long id) throws NotFoundException {
        if (!existsById(id)) {
            throw new NotFoundException("Отзыв с ID " + id + " не найден");
        }
    }
}
