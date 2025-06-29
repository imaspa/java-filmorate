package ru.yandex.practicum.filmorate.data.repository;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.data.model.Film;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class FilmRepository extends BaseRepository<Film> {

    public List<Film> findPopular(Long limit) {
        return findAll()
                .stream()
                .sorted(Comparator.comparing((Film film) -> film.getLikes().size()).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
}
