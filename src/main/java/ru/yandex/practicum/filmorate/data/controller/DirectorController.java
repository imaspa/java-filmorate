package ru.yandex.practicum.filmorate.data.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.data.dto.DirectorDto;
import ru.yandex.practicum.filmorate.data.exception.ConditionsException;
import ru.yandex.practicum.filmorate.data.exception.NotFoundException;
import ru.yandex.practicum.filmorate.data.service.DirectorService;

import java.util.Collection;

@RestController
@RequestMapping("/directors")
@RequiredArgsConstructor
public class DirectorController {
    private final DirectorService service;

    @PostMapping
    public DirectorDto add(@RequestBody DirectorDto directorDto) throws ConditionsException {
        return service.add(directorDto);
    }

    @GetMapping
    public Collection<DirectorDto> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public DirectorDto getDirectorById(@PathVariable Long id) throws NotFoundException {
        return service.getDirectorById(id);
    }

    @PutMapping
    public DirectorDto update(@RequestBody DirectorDto directorDto) throws ConditionsException, NotFoundException {
        return service.update(directorDto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
