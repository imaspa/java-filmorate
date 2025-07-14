package ru.yandex.practicum.filmorate.data.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import ru.yandex.practicum.filmorate.data.config.CommonMapperConfiguration;
import ru.yandex.practicum.filmorate.data.dto.EventLogDto;
import ru.yandex.practicum.filmorate.data.model.EventLog;

import java.time.LocalDate;
import java.time.ZoneOffset;

@Mapper(config = CommonMapperConfiguration.class)
public interface EventLogMapper {
    @Mapping(target = "id", ignore = true)
    EventLog map(@MappingTarget EventLog entity, EventLogDto dto);

    @Mapping(target = "id", ignore = true)
    EventLog toEntity(EventLogDto dto);

    @Mapping(target = "eventId", source = "id")
    @Mapping(target = "timestamp", source = "eventDate", qualifiedByName = "localDateToTimestamp")
    EventLogDto toDto(EventLog entity);

    @Named("localDateToTimestamp")
    static long localDateToTimestamp(LocalDate date) {
        if (date == null) {
            return 0L;
        }
        return date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
    }
}