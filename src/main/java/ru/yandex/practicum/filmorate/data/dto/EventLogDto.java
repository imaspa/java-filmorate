package ru.yandex.practicum.filmorate.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.data.model.constant.EventType;
import ru.yandex.practicum.filmorate.data.model.constant.Operation;

@Builder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventLogDto {

    private Long eventId;

    private Long timestamp;

    private Long userId;

    private EventType eventType;

    private Operation operation;

    private Long entityId;
}
