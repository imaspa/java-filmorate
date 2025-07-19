package ru.yandex.practicum.filmorate.data.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.data.constant.EventType;
import ru.yandex.practicum.filmorate.data.constant.Operation;
import ru.yandex.practicum.filmorate.data.exception.ConditionsException;
import ru.yandex.practicum.filmorate.data.exception.NotFoundException;
import ru.yandex.practicum.filmorate.data.repository.FriendshipRepository;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class FriendshipService {
    private final FriendshipRepository repository;
    private final EventLogService eventLogService;

    public void addFriend(Long userId, Long friendId, Boolean isFriend) throws ConditionsException {
        repository.addFriend(userId, friendId, isFriend);
        eventLogService.add(userId, friendId, EventType.FRIEND, Operation.ADD);
    }

    public void removeFriend(Long userId, Long friendId) throws NotFoundException, ConditionsException {
        var friendship = repository.findByUserIdAndFriendId(userId, friendId).orElse(null);
        if (friendship == null) return;
        eventLogService.add(userId, friendId, EventType.FRIEND, Operation.REMOVE);
        repository.removeFriend(friendship.getId());
    }
}
