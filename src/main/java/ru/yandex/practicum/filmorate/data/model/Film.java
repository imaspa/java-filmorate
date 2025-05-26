package ru.yandex.practicum.filmorate.data.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Builder
@Data
public class Film implements Identifiable {

    private Long id;

    private String name;

    private String description;

    private LocalDate releaseDate;

    private Integer duration;
}
