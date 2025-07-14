package ru.yandex.practicum.filmorate.data.controller.handler;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ErrorMessage {
    private final boolean error = true;
    private String message;
}