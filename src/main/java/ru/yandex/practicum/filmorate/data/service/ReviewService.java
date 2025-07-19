package ru.yandex.practicum.filmorate.data.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.data.constant.EventType;
import ru.yandex.practicum.filmorate.data.constant.Operation;
import ru.yandex.practicum.filmorate.data.dto.ReviewDto;
import ru.yandex.practicum.filmorate.data.exception.ConditionsException;
import ru.yandex.practicum.filmorate.data.exception.NotFoundException;
import ru.yandex.practicum.filmorate.data.mapper.ReviewMapper;
import ru.yandex.practicum.filmorate.data.model.Review;
import ru.yandex.practicum.filmorate.data.repository.FilmRepository;
import ru.yandex.practicum.filmorate.data.repository.ReviewRepository;
import ru.yandex.practicum.filmorate.data.repository.UserRepository;

import java.util.List;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository repository;
    private final ReviewMapper mapper;
    private final FilmRepository repositoryFilm;
    private final UserRepository repositoryUser;
    private final EventLogService eventLogService;

    public ReviewDto add(@Valid ReviewDto reviewDto) throws ConditionsException, NotFoundException {
        log.info("Создание отзыва (старт) review: {}", reviewDto);
        validate(reviewDto);
        Review review = mapper.toEntity(reviewDto);
        review = repository.insert(review);
        eventLogService.add(reviewDto.getUserId(), review.getId(), EventType.REVIEW, Operation.ADD);
        log.info("Создание отзыва (стоп) review: {}", review);
        return mapper.toDto(review);
    }

    public ReviewDto update(@Valid ReviewDto reviewDto) throws ConditionsException, NotFoundException {
        log.info("Редактирование отзыва (старт) review: {}", reviewDto);
        validate(reviewDto);
        Review review = mapper.mapUpdate(repository.findByIdOrThrow(reviewDto.getReviewId()), reviewDto);
        repository.update(review);
        eventLogService.add(review.getUserId(), review.getId(), EventType.REVIEW, Operation.UPDATE);
        log.info("Редактирование отзыва (стоп) review: {}", review);
        return mapper.toDto(review);
    }

    public void delete(Long reviewId) throws NotFoundException, ConditionsException {
        log.info("Удаление отзыва (старт) id: {}", reviewId);
        Review review = repository.findByIdOrThrow(reviewId);
        int count = repository.deleteById(reviewId);
        eventLogService.add(review.getUserId(), reviewId, EventType.REVIEW, Operation.REMOVE);
        log.info("Удаление отзыва (стоп) id: {}, удалено {}", reviewId, count);
    }

    public ReviewDto getById(Long id) throws NotFoundException {
        log.info("Получение данных отзыва (старт) id = {}", id);
        Review review = repository.findByIdOrThrow(id);
        log.info("Получение данных отзыва (стоп) id = {}", id);
        return mapper.toDto(review);
    }

    public List<ReviewDto> getAll(Long filmId, Long count) {
        log.info("Запрос всех отзывов по фильму id = {}, количеством {}", filmId, count);
        return repository.getAll(filmId, count).stream().map(mapper::toDto).toList();
    }

    public void validate(ReviewDto reviewDto) throws NotFoundException, ConditionsException {
        Long userId = reviewDto.getUserId();
        Long filmId = reviewDto.getFilmId();
        if (userId == null) {
            throw new ConditionsException("Юзер должен быть указан");
        }
        if (filmId == null) {
            throw new ConditionsException("Фильм должен быть указан");
        }
        repositoryUser.checkExists(userId);
        repositoryFilm.checkExists(filmId);
        if (reviewDto.getIsPositive() == null) {
            throw new ConditionsException("Тип отзыва должен быть либо позитивный, либо негативный");
        }
        if (reviewDto.getUseful() == null) {
            reviewDto.setUseful(0);
        }
    }

    private void validateExistsReviewAndUser(Long filmId, Long userId) throws NotFoundException {
        repositoryUser.checkExists(userId);
        repository.checkExists(filmId);
    }

    public ReviewDto addLike(Long reviewId, Long userId, boolean isDislike) throws NotFoundException, ConditionsException {
        log.info("Добавление реакции на отзыв (старт) id = {}, юзер = {}, dislike = {}", reviewId, userId, isDislike);
        validateExistsReviewAndUser(reviewId, userId);
        repository.addLike(reviewId, userId, isDislike);
        Review review = repository.findByIdOrThrow(reviewId);
        log.info("Добавлена реакция isDislike = {}, useful теперь {}", isDislike, review.getUseful());
        return mapper.toDto(review); //обновление useful
    }

    public void deleteLike(Long reviewId, Long userId) throws NotFoundException, ConditionsException {
        log.info("Удаление реакции на отзыв (старт) id = {}, юзер = {}", reviewId, userId);
        validateExistsReviewAndUser(reviewId, userId);
        Boolean isDislike = repository.getIsDislike(reviewId, userId);
        if (isDislike == null) {
            log.info("Не найдена реакция на отзыв {} юзера {}", reviewId, userId);
            return;
        }
        repository.deleteLike(reviewId, userId, isDislike);
        log.info("Удаление реакции на отзыв (стоп) id = {}, юзер = {}", reviewId, userId);
    }
}
