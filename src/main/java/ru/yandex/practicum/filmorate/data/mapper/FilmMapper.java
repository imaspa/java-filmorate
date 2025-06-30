package ru.yandex.practicum.filmorate.data.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.yandex.practicum.filmorate.data.config.CommonMapperConfiguration;
import ru.yandex.practicum.filmorate.data.dto.FilmDto;
import ru.yandex.practicum.filmorate.data.model.Film;

@Mapper(config = CommonMapperConfiguration.class)
public interface FilmMapper {
    @Mapping(target = "id", ignore = true)
    void map(@MappingTarget Film entity, FilmDto dto);

    @Mapping(target = "id", ignore = true)
    Film toEntity(FilmDto dto);

    FilmDto toDto(Film entity);
}
