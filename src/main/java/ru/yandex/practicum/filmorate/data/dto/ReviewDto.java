package ru.yandex.practicum.filmorate.data.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDto {

    private Long reviewId;

    private Long userId;

    private Long filmId;

    @NotNull(message = "Отзыв не может быть пустым")
    @NotBlank(message = "Отзыв не может быть пустым")
    private String content;

    private Boolean isPositive;

    private Integer useful;
}
