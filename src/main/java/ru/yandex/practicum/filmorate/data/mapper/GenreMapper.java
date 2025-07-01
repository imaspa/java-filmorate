package ru.yandex.practicum.filmorate.data.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.filmorate.data.config.CommonMapperConfiguration;
import ru.yandex.practicum.filmorate.data.dto.GenreDto;
import ru.yandex.practicum.filmorate.data.model.Genre;

@Mapper(config = CommonMapperConfiguration.class)
public interface GenreMapper {

    @Mapping(target = "id", ignore = true)
    Genre toEntity(GenreDto dto);

    GenreDto toDto(Genre entity);
}
