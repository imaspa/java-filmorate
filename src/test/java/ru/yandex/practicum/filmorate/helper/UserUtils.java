package ru.yandex.practicum.filmorate.helper;

import ru.yandex.practicum.filmorate.data.dto.UserDto;

import java.time.LocalDate;

public final class UserUtils {
    public static UserDto createUserValid() {
        return UserDto.builder()
                .name("Имя")
                .login("Логин")
                .email("test@test.ru")
                .birthday(LocalDate.of(1980, 1, 1))
                .build();
    }

    public static UserDto createUserEmailEmpty() {
        return createUserValid().toBuilder()
                .email("")
                .build();
    }

    public static UserDto createUserEmailInvalid() {
        return createUserValid().toBuilder()
                .email("test@")
                .build();
    }

    public static UserDto createUserLoginEmpty() {
        return createUserValid().toBuilder()
                .login("")
                .build();
    }

    public static UserDto createUserLoginWithSpace() {
        return createUserValid().toBuilder()
                .login("")
                .build();
    }

    public static UserDto createUserBirthdayFuture() {
        return createUserValid().toBuilder()
                .birthday(LocalDate.now().plusDays(1))
                .build();
    }
}