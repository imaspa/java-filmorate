package ru.yandex.practicum.filmorate.data.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.data.dto.ReviewDto;
import ru.yandex.practicum.filmorate.data.exception.ConditionsException;
import ru.yandex.practicum.filmorate.data.mapper.ReviewMapper;
import ru.yandex.practicum.filmorate.data.model.Review;
import ru.yandex.practicum.filmorate.data.repository.ReviewRepository;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository repository;
    private final ReviewMapper mapper;

    public ReviewDto add(ReviewDto reviewDto) throws ConditionsException {
        log.info("Создание отзыва (старт) review: {}", reviewDto);
        Review review = mapper.toEntity(reviewDto);
        repository.insert(review);
        log.info("Создание отзыва (стоп) review: {}", review);
        return mapper.toDto(review);
    }
}
