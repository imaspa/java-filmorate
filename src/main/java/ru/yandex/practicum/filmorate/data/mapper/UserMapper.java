package ru.yandex.practicum.filmorate.data.mapper;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.filmorate.data.config.CommonMapperConfiguration;
import ru.yandex.practicum.filmorate.data.dto.UserDto;
import ru.yandex.practicum.filmorate.data.model.User;

@Mapper(config = CommonMapperConfiguration.class)
public interface UserMapper {
    @Mapping(target = "id", expression = "java(id)")
    User toEntity(UserDto userDto, @Context Long id);

    UserDto toDto(User film);
}
