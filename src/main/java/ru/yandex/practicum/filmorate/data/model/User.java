package ru.yandex.practicum.filmorate.data.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class User implements Identifiable {

    private Long id;

    private String email;

    private String login;

    private String name;

    private LocalDate birthday;
}
