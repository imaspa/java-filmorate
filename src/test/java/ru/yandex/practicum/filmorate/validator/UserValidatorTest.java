package ru.yandex.practicum.filmorate.validator;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.data.dto.UserDto;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.yandex.practicum.filmorate.helper.BaseUtils.containsValidationMessage;
import static ru.yandex.practicum.filmorate.helper.UserUtils.createUserBirthdayFuture;
import static ru.yandex.practicum.filmorate.helper.UserUtils.createUserEmailEmpty;
import static ru.yandex.practicum.filmorate.helper.UserUtils.createUserEmailInvalid;
import static ru.yandex.practicum.filmorate.helper.UserUtils.createUserLoginEmpty;
import static ru.yandex.practicum.filmorate.helper.UserUtils.createUserLoginWithSpace;
import static ru.yandex.practicum.filmorate.helper.UserUtils.createUserValid;

@DisplayName("Проверка валидации UserDto")
public class UserValidatorTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    @DisplayName("Проверка валидного пользователя")
    void whenValidUserDtoThenNoViolations() {
        UserDto user = createUserValid();
        Set<ConstraintViolation<UserDto>> violations = validator.validate(user);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Пустой логин должен вызывать ошибку")
    void whenUserLoginEmptyThenViolations() {
        String messageError = "Логин не может быть пустым";
        UserDto user = createUserLoginEmpty();
        Set<ConstraintViolation<UserDto>> violations = validator.validate(user);
        boolean isFoundExpectedError = containsValidationMessage(violations, messageError);
        assertTrue(isFoundExpectedError, "Среди ошибок валидации должна быть: '%s'. ".formatted(messageError));
    }

    @Test
    @DisplayName("Логин с пробелом должен вызывать ошибку")
    void whenUserLoginWithSpaceThenViolations() {
        String messageError = "Логин не может содержать пробелы";
        UserDto user = createUserLoginWithSpace();
        Set<ConstraintViolation<UserDto>> violations = validator.validate(user);
        boolean isFoundExpectedError = containsValidationMessage(violations, messageError);
        assertTrue(isFoundExpectedError, "Среди ошибок валидации должна быть: '%s'. ".formatted(messageError));
    }

    @Test
    @DisplayName("Пустой email должен вызывать ошибку")
    void whenUserEmailWithSpaceThenViolations() {
        String messageError = "Адрес электронной почты не может быть пустой";
        UserDto user = createUserEmailEmpty();
        Set<ConstraintViolation<UserDto>> violations = validator.validate(user);
        boolean isFoundExpectedError = containsValidationMessage(violations, messageError);
        assertTrue(isFoundExpectedError, "Среди ошибок валидации должна быть: '%s'. ".formatted(messageError));
    }

    @Test
    @DisplayName("Не валидный email должен вызывать ошибку")
    void whenUserEmailInvalidThenViolations() {
        String messageError = "Адрес электронной почты должен быть валидным";
        UserDto user = createUserEmailInvalid();
        Set<ConstraintViolation<UserDto>> violations = validator.validate(user);
        boolean isFoundExpectedError = containsValidationMessage(violations, messageError);
        assertTrue(isFoundExpectedError, "Среди ошибок валидации должна быть: '%s'. ".formatted(messageError));
    }

    @Test
    @DisplayName("Дата рождения в 'будущем' должна вызвать ошибку")
    void whenUserBirthdayBirthdayThenViolations() {
        String messageError = "Дата рождения не может быть в будущем";
        UserDto user = createUserBirthdayFuture();
        Set<ConstraintViolation<UserDto>> violations = validator.validate(user);
        boolean isFoundExpectedError = containsValidationMessage(violations, messageError);
        assertTrue(isFoundExpectedError, "Среди ошибок валидации должна быть: '%s'. ".formatted(messageError));
    }

}
