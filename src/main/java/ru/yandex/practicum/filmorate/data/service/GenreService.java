package ru.yandex.practicum.filmorate.data.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.data.dto.GenreDto;
import ru.yandex.practicum.filmorate.data.exception.NotFoundException;
import ru.yandex.practicum.filmorate.data.mapper.GenreMapper;
import ru.yandex.practicum.filmorate.data.repository.GenreRepository;

import java.util.List;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class GenreService {
    private final GenreRepository repository;
    private final GenreMapper mapper;

    public List<GenreDto> getAll() {
        log.info("Получение списка всех жанров (старт)");
        var result = repository.findAll()
                .stream()
                .map(mapper::toDto)
                .toList();
        log.info("Получение списка всех жанров (стоп)");
        return result;
    }

    public GenreDto getEntity(Long id) throws NotFoundException {
        return mapper.toDto(repository.findByIdOrThrow(id));
    }


}
