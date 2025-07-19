package ru.yandex.practicum.filmorate.data.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DirectorSortValue {
    DEFAULT("ID"),
    year("YEARS"),
    likes("LIKES DESC");
    private final String sqlOrder;
}
