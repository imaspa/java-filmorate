package ru.yandex.practicum.filmorate.data.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.yandex.practicum.filmorate.data.config.CommonMapperConfiguration;
import ru.yandex.practicum.filmorate.data.dto.DirectorDto;
import ru.yandex.practicum.filmorate.data.model.Director;

@Mapper(config = CommonMapperConfiguration.class)
public interface DirectorMapper {
    @Mapping(target = "id", ignore = true)
    Director map(@MappingTarget Director entity, DirectorDto dto);

    @Mapping(target = "id", ignore = true)
    Director toEntity(DirectorDto dto);

    @Mapping(target = "id", source = "id")
    DirectorDto toDto(Director entity);
}
