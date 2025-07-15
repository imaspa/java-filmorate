package ru.yandex.practicum.filmorate.data.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.data.dto.EventLogDto;
import ru.yandex.practicum.filmorate.data.dto.FilmDto;
import ru.yandex.practicum.filmorate.data.dto.UserDto;
import ru.yandex.practicum.filmorate.data.exception.ConditionsException;
import ru.yandex.practicum.filmorate.data.exception.NotFoundException;
import ru.yandex.practicum.filmorate.data.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public UserDto addUser(@RequestBody UserDto userDto) throws ConditionsException {
        return userService.add(userDto);
    }

    @PutMapping("/{userId}")
    public UserDto updateUser(@PathVariable Long userId, @RequestBody UserDto userDto) throws NotFoundException, ConditionsException {
        return userService.update(userId, userDto);
    }

    @PutMapping
    public UserDto updateUser(@RequestBody UserDto userDto) throws NotFoundException, ConditionsException {
        return userService.update(userDto.getId(), userDto);
    }

    @GetMapping
    public Iterable<UserDto> getUser() {
        return userService.getAll();
    }

    @GetMapping("/{userId}")
    public UserDto getUser(@PathVariable Long userId) throws NotFoundException {
        return userService.getUser(userId);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable Long id, @PathVariable Long friendId) throws ConditionsException, NotFoundException {
        userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{userId}/friends/{friendId}")
    public void removeFriend(@PathVariable Long userId, @PathVariable Long friendId) throws NotFoundException, ConditionsException {
        userService.removeFriend(userId, friendId);
    }

    @GetMapping("/{id}/friends")
    public List<UserDto> getFriendsList(@PathVariable Long id) throws NotFoundException {
        return userService.getUserFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<UserDto> getCommonFriends(@PathVariable Long id, @PathVariable Long otherId) throws ConditionsException, NotFoundException {
        return userService.getCommonFriends(id, otherId);
    }

    @GetMapping("/{id}/recommendations")
    public List<FilmDto> getRecommendations(@PathVariable Long id, @RequestParam(required = false, defaultValue = "10") Integer limit) throws NotFoundException {
        return userService.getRecommendations(id, limit);
    }

    @GetMapping("/{userId}/feed")
    public List<EventLogDto> getFeedByUserId(@PathVariable Long userId, @RequestParam(defaultValue = "100") Long limit) throws NotFoundException {
        return userService.getFeedByUserId(userId, limit);
    }


}
