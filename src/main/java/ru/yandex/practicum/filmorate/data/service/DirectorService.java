package ru.yandex.practicum.filmorate.data.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.data.dto.DirectorDto;
import ru.yandex.practicum.filmorate.data.exception.ConditionsException;
import ru.yandex.practicum.filmorate.data.exception.NotFoundException;
import ru.yandex.practicum.filmorate.data.mapper.DirectorMapper;
import ru.yandex.practicum.filmorate.data.model.Director;
import ru.yandex.practicum.filmorate.data.repository.DirectorRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Validated
public class DirectorService {
    private final DirectorRepository repository;
    private final DirectorMapper mapper;

    public DirectorDto add(@Valid DirectorDto directorDto) throws ConditionsException {
        log.info("Создание режиссера (старт) имя = {}", directorDto.getName());
        Director director = mapper.toEntity(directorDto);
        director = repository.insert(director);
        log.info("Создание режиссера (старт) имя = {}, id ={}", director.getName(), director.getId());
        return mapper.toDto(director);
    }

    public List<DirectorDto> getAll() {
        log.info("Получение списка режиссеров");
        return repository.getAll().stream().map(mapper::toDto).toList();
    }

    public DirectorDto getDirectorById(Long id) throws NotFoundException {
        log.info("Получение режиссера по id = {}", id);
        Director director = repository.getDirectorById(id);
        return mapper.toDto(director);
    }

    public DirectorDto update(@Valid DirectorDto directorDto) throws NotFoundException, ConditionsException {
        log.info("Изменение режиссера (старт) id = {}, name = {}", directorDto.getId(), directorDto.getName());
        var director = repository.getDirectorById(directorDto.getId());
        director = mapper.map(director, directorDto);
        log.info("Изменение режиссера (стоп) id = {}, name = {}", directorDto.getId(), directorDto.getName());
        return mapper.toDto(repository.update(director));

    }

    public void delete(Long id) {
        log.info("Удаление режиссера (старт) id = {}", id);
        repository.deleteById(id);
        log.info("Удаление режиссера (стоп) id = {}", id);
    }
}
