package ru.yandex.practicum.filmorate.data.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review implements Identifiable {
    private Long id;
    private Long userId;
    private Long filmId;
    private String content;
    private Boolean isPositive = true;
    private Integer useful = 0;
}
