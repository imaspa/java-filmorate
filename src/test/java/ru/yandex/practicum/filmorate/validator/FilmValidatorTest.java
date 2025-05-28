package ru.yandex.practicum.filmorate.validator;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.data.dto.FilmDto;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.yandex.practicum.filmorate.helper.BaseUtils.containsValidationMessage;
import static ru.yandex.practicum.filmorate.helper.FilmUtils.createFilmDescriptionGT200;
import static ru.yandex.practicum.filmorate.helper.FilmUtils.createFilmDurationZero;
import static ru.yandex.practicum.filmorate.helper.FilmUtils.createFilmNameEmpty;
import static ru.yandex.practicum.filmorate.helper.FilmUtils.createFilmReleaseDateBefore1895_12_28;
import static ru.yandex.practicum.filmorate.helper.FilmUtils.createFilmReleaseDateEmpty;
import static ru.yandex.practicum.filmorate.helper.FilmUtils.createFilmValid;

@DisplayName("Проверка валидации FilmDto")
public class FilmValidatorTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    @DisplayName("Проверка валидного фильма")
    void whenValidFilmDtoThenNoViolations() {
        FilmDto film = createFilmValid();
        Set<ConstraintViolation<FilmDto>> violations = validator.validate(film);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Пустое название фильма должно вызывать ошибку")
    void whenFilmNameEmptyThenViolations() {
        String messageError = "Название фильма не может быть пустым";
        FilmDto film = createFilmNameEmpty();
        Set<ConstraintViolation<FilmDto>> violations = validator.validate(film);
        boolean isFoundExpectedError = containsValidationMessage(violations, messageError);
        assertTrue(isFoundExpectedError, "Среди ошибок валидации должна быть: '%s'. ".formatted(messageError));
    }

    @Test
    @DisplayName("Описание больше 200 символов должно вызывать ошибку")
    void whenFilmDescriptionGT200ThenViolations() {
        String messageError = "Максимальная длина описания — 200 символов";
        FilmDto film = createFilmDescriptionGT200();
        Set<ConstraintViolation<FilmDto>> violations = validator.validate(film);
        boolean isFoundExpectedError = containsValidationMessage(violations, messageError);
        assertTrue(isFoundExpectedError, "Среди ошибок валидации должна быть: '%s'. ".formatted(messageError));
    }

    @Test
    @DisplayName("Пустая дата релиза должна вызывать ошибку")
    void whenFilmReleaseDateEmptyThenViolations() {
        String messageError = "Дата релиза не может быть пустой";
        FilmDto film = createFilmReleaseDateEmpty();
        Set<ConstraintViolation<FilmDto>> violations = validator.validate(film);
        boolean isFoundExpectedError = containsValidationMessage(violations, messageError);
        assertTrue(isFoundExpectedError, "Среди ошибок валидации должна быть: '%s'. ".formatted(messageError));
    }

    @Test
    @DisplayName("Дата релиза раньше 1895-12-28 должна вызывать ошибку")
    void whenFilmReleaseDateBefore1895_12_28ThenViolations() {
        String messageError = "Дата релиза фильма не может быть раньше 1895-12-28";
        FilmDto film = createFilmReleaseDateBefore1895_12_28();
        Set<ConstraintViolation<FilmDto>> violations = validator.validate(film);
        boolean isFoundExpectedError = containsValidationMessage(violations, messageError);
        assertTrue(isFoundExpectedError, "Среди ошибок валидации должна быть: '%s'. ".formatted(messageError));
    }

    @Test
    @DisplayName("Продолжительность фильма меньше 1 секунды должна вызывать ошибку")
    void whenFilmDurationZeroThenViolations() {
        String messageError = "Продолжительность фильма должна быть положительным числом";
        FilmDto film = createFilmDurationZero();
        Set<ConstraintViolation<FilmDto>> violations = validator.validate(film);
        boolean isFoundExpectedError = containsValidationMessage(violations, messageError);
        assertTrue(isFoundExpectedError, "Среди ошибок валидации должна быть: '%s'. ".formatted(messageError));
    }


}
