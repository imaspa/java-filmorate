package ru.yandex.practicum.filmorate.data.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.yandex.practicum.filmorate.data.config.CommonMapperConfiguration;
import ru.yandex.practicum.filmorate.data.dto.ReviewDto;
import ru.yandex.practicum.filmorate.data.model.Review;

@Mapper(config = CommonMapperConfiguration.class)
public interface ReviewMapper {
    @Mapping(target = "id", ignore = true)
    Review map(@MappingTarget Review entity, ReviewDto dto);

    @Mapping(target = "id", ignore = true)
    Review toEntity(ReviewDto dto);

    @Mapping(target = "reviewId", source = "id")
    ReviewDto toDto(Review entity);
}
