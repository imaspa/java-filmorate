package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.filmorate.data.exception.NotFoundException;
import ru.yandex.practicum.filmorate.data.model.MpaRating;
import ru.yandex.practicum.filmorate.data.repository.MpaRatingRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(MpaRatingRepository.class)
class MpaRatingRepositoryIT {

    @Autowired
    private MpaRatingRepository mpaRatingRepository;

    @Test
    void shouldFindAllMpaRatings() {
        List<MpaRating> ratings = mpaRatingRepository.findAll();
        assertThat(ratings).hasSize(5); // Из data.sql
        assertThat(ratings.get(0).getName()).isEqualTo("G");
    }

    @Test
    void shouldFindMpaRatingById() throws NotFoundException {
        MpaRating rating = mpaRatingRepository.findByIdOrThrow(1L);
        assertThat(rating.getName()).isEqualTo("G");
    }

    @Test
    void shouldCheckIfMpaRatingExists() {
        assertThat(mpaRatingRepository.existsById(1L)).isTrue();
        assertThat(mpaRatingRepository.existsById(999L)).isFalse();
    }
}