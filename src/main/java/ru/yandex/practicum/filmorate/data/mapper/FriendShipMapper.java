package ru.yandex.practicum.filmorate.data.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.yandex.practicum.filmorate.data.config.CommonMapperConfiguration;
import ru.yandex.practicum.filmorate.data.dto.FriendshipDto;
import ru.yandex.practicum.filmorate.data.model.Friendship;

@Mapper(config = CommonMapperConfiguration.class)
public interface FriendShipMapper {

    @Mapping(target = "id", ignore = true)
    Friendship map(@MappingTarget Friendship entity, FriendshipDto dto);

    @Mapping(target = "id", ignore = true)
    Friendship toEntity(FriendshipDto dto);

    FriendshipDto toDto(Friendship entity);
}
