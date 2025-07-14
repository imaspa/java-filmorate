package ru.yandex.practicum.filmorate.data.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.data.exception.ConditionsException;
import ru.yandex.practicum.filmorate.data.model.constant.EventType;
import ru.yandex.practicum.filmorate.data.model.constant.Operation;
import ru.yandex.practicum.filmorate.data.repository.EventLogRepository;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class EventLogService {
    private final EventLogRepository repository;

    public void add(Long userId, Long entityId, EventType eventType, Operation operation) throws ConditionsException {
        repository.insert(userId, entityId, eventType, operation);
    }
}
