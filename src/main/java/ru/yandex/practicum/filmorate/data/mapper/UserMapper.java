package ru.yandex.practicum.filmorate.data.mapper;

import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import ru.yandex.practicum.filmorate.data.config.CommonMapperConfiguration;
import ru.yandex.practicum.filmorate.data.dto.UserDto;
import ru.yandex.practicum.filmorate.data.model.User;

@Mapper(config = CommonMapperConfiguration.class)
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    User map(@MappingTarget User entity, UserDto dto);

    UserDto toDto(User film);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", source = "dto", qualifiedByName = "prepareName")
    User toEntity(UserDto dto);

    @Named("prepareName")
    default String prepareName(UserDto dto) {
        return StringUtils.isBlank(dto.getName()) ? dto.getLogin() : dto.getName();
    }
}
