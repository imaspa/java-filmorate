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
import ru.yandex.practicum.filmorate.data.model.MpaRating;
import ru.yandex.practicum.filmorate.data.model.Review;
import ru.yandex.practicum.filmorate.data.model.User;
import ru.yandex.practicum.filmorate.data.repository.FilmRepository;
import ru.yandex.practicum.filmorate.data.repository.MpaRatingRepository;
import ru.yandex.practicum.filmorate.data.repository.ReviewRepository;
import ru.yandex.practicum.filmorate.data.repository.UserRepository;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import({FilmRepository.class, UserRepository.class, MpaRatingRepository.class, ReviewRepository.class})
class ReviewRepositoryT {
    @Autowired
    private FilmRepository filmRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MpaRatingRepository mpaRatingRepository;
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Film film;
    private User user1;
    private User user2;

    @BeforeEach
    void setUp() throws ConditionsException, NotFoundException {
        jdbcTemplate.update("DELETE FROM REVIEW_LIKE");
        jdbcTemplate.update("DELETE FROM REVIEW");
        jdbcTemplate.update("DELETE FROM FILM");
        jdbcTemplate.update("DELETE FROM USERS");
        MpaRating existingMpa = mpaRatingRepository.findByIdOrThrow(1L);
        film = Film.builder()
                .name("Film 1")
                .description("Description")
                .releaseDate(LocalDate.of(1990, 1, 1))
                .duration(100)
                .mpa(existingMpa)
                .build();
        film = filmRepository.insert(film);
        user1 = User.builder()
                .name("User 1")
                .login("user1")
                .email("email1@yamdex.ru")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
        user1 = userRepository.insert(user1);
        user2 = User.builder()
                .name("User 2")
                .login("user2")
                .email("email2@yamdex.ru").birthday(LocalDate.of(1990, 1, 1))
                .build();
        user2 = userRepository.insert(user2);
    }

    @Test
    public void shouldCreateAndFindReview() throws ConditionsException, NotFoundException {
        Review review = Review.builder()
                .filmId(film.getId())
                .userId(user1.getId())
                .content("Film is greate")
                .isPositive(true)
                .build();
        Review savedReview = reviewRepository.insert(review);
        Review foundReview = reviewRepository.findByIdOrThrow(savedReview.getId());
        assertEquals(foundReview.getId(), savedReview.getId(), "Не создался отзыв");
    }

    @Test
    public void shouldAddLikeAndDislikeCorrectly() throws ConditionsException, NotFoundException {
        Review review = Review.builder()
                .filmId(film.getId())
                .userId(user1.getId())
                .content("Film is greate")
                .isPositive(true)
                .build();
        Review savedReview = reviewRepository.insert(review);
        Long reviewId = savedReview.getId();
        reviewRepository.addLike(reviewId, user1.getId(), false);
        reviewRepository.addLike(reviewId, user2.getId(), false);
        Review reviewAfterTwoLikes = reviewRepository.findByIdOrThrow(reviewId);
        reviewRepository.addLike(reviewId, user2.getId(), true);
        Review reviewAfterDislike = reviewRepository.findByIdOrThrow(reviewId);
        assertEquals(2, reviewAfterTwoLikes.getUseful(), "Некорректно посчитались лайки");
        assertEquals(0, reviewAfterDislike.getUseful(), "Некорректно посчитались дизлайки");
    }
}