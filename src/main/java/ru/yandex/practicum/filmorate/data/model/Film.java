package ru.yandex.practicum.filmorate.data.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Film implements Identifiable {

    private Long id;

    private String name;

    private String description;

    private LocalDate releaseDate;

    private int duration;

    private MpaRating mpa;

    private Set<Genre> genres;

    private Set<Long> likes;

    private Set<Director> directors;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        Film film = (Film) o;
        return Objects.equals(id, film.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public Set<Genre> getGenres() {
        if (genres == null) {
            genres = new HashSet<>();
        }
        return genres;
    }

    public Set<Long> getLikes() {
        if (likes == null) {
            likes = new HashSet<>();
        }
        return likes;
    }
}
