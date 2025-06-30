package ru.yandex.practicum.filmorate.data.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.filmorate.data.config.CommonMapperConfiguration;
import ru.yandex.practicum.filmorate.data.dto.MpaRatingDto;
import ru.yandex.practicum.filmorate.data.model.MpaRating;

@Mapper(config = CommonMapperConfiguration.class)
public interface MpaRatingMapper {

    @Mapping(target = "id", ignore = true)
    MpaRating toEntity(MpaRatingDto dto);

    MpaRatingDto toDto(MpaRating entity);
}
