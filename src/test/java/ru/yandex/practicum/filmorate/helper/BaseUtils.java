package ru.yandex.practicum.filmorate.helper;

import jakarta.validation.ConstraintViolation;

import java.util.Set;

public final class BaseUtils {
    public static <T> boolean containsValidationMessage(Set<ConstraintViolation<T>> violations, String expectedMessage) {
        return violations.stream()
                .map(ConstraintViolation::getMessage)
                .anyMatch(expectedMessage::equals);
    }

}
