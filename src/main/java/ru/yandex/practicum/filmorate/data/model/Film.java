package ru.yandex.practicum.filmorate.data.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Builder
@Data
public class Film implements Identifiable {

    private final Set<Long> likes = new HashSet<>();
    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;

    public Boolean isLiked(Long userId) {
        return likes.contains(userId);
    }

    public void addLike(Long userId) {
        likes.add(userId);
    }

    public void removeLike(Long userId) {
        likes.remove(userId);
    }
}
