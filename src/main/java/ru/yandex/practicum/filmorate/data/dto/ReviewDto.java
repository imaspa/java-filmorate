package ru.yandex.practicum.filmorate.data.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDto {
    private Long id;
    private Long userId;
    private Long filmId;
    @NotBlank(message = "Отзыв не может быть пустым")
    private String content;
    private Boolean isPositive;
    private Integer useful;
}
