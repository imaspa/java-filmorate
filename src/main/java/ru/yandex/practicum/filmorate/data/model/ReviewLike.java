package ru.yandex.practicum.filmorate.data.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewLike {
    private Long userId;
    private Long reviewId;
    private Boolean isDislike = false;
}
