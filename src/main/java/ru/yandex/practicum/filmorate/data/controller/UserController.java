package ru.yandex.practicum.filmorate.data.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.data.dto.UserDto;
import ru.yandex.practicum.filmorate.data.exception.IdNotEmptyException;
import ru.yandex.practicum.filmorate.data.exception.NotFoundException;
import ru.yandex.practicum.filmorate.data.service.UserService;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public UserDto addUser(@RequestBody UserDto userDto) throws IdNotEmptyException {
        return userService.add(userDto);
    }

    @PutMapping("/{userId}")
    public UserDto updateUser(@PathVariable Long userId, @RequestBody UserDto userDto) throws NotFoundException {
        return userService.update(userId,userDto);
    }

    @PutMapping
    public UserDto updateUser(@RequestBody UserDto userDto) throws NotFoundException {
        return userService.update(userDto.getId(),userDto);
    }

    @GetMapping
    public Iterable<UserDto> getUser() {
        return userService.getAll();
    }

}
