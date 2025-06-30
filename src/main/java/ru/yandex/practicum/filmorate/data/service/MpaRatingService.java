package ru.yandex.practicum.filmorate.data.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.data.dto.MpaRatingDto;
import ru.yandex.practicum.filmorate.data.exception.NotFoundException;
import ru.yandex.practicum.filmorate.data.mapper.MpaRatingMapper;
import ru.yandex.practicum.filmorate.data.repository.MpaRatingRepository;

import java.util.List;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class MpaRatingService {
    private final MpaRatingRepository repository;
    private final MpaRatingMapper mapper;

    public List<MpaRatingDto> getAll() {
        log.info("Получение списка всех рейтингов  (старт)");
        var result = repository.findAll()
                .stream()
                .map(mapper::toDto)
                .toList();
        log.info("Получение списка всех рейтингов  (стоп)");
        return result;
    }

    public MpaRatingDto getEntity(Long id) throws NotFoundException {
        return mapper.toDto(repository.findByIdOrThrow(id));
    }


}
