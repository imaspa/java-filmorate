package ru.yandex.practicum.filmorate.data.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.data.constant.EventType;
import ru.yandex.practicum.filmorate.data.constant.Operation;

import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class EventLog implements Identifiable {

    private Long id;

    private LocalDate eventDate;

    private Long userId;

    private EventType eventType;

    private Operation operation;

    private Long entityId;

}
