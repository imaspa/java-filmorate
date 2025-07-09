package ru.yandex.practicum.filmorate.data.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.data.exception.ConditionsException;
import ru.yandex.practicum.filmorate.data.model.Review;

import java.sql.Date;

@Repository
public class ReviewRepository extends BaseRepository<Review> {
    private final JdbcTemplate jdbcTemplate;
    private static final String INSERT_SQL = "INSERT INTO REVIEW (user_id,film_id,content,is_positive,useful) VALUES (?,?,?,?,?)";

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
            ps.setInt(5, f.getUseful());
        }, review);
        return savedReview;
    }
}
