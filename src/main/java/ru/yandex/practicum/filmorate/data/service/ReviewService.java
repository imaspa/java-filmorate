package ru.yandex.practicum.filmorate.data.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.data.dto.ReviewDto;
import ru.yandex.practicum.filmorate.data.exception.ConditionsException;
import ru.yandex.practicum.filmorate.data.exception.NotFoundException;
import ru.yandex.practicum.filmorate.data.mapper.ReviewMapper;
import ru.yandex.practicum.filmorate.data.model.Review;
import ru.yandex.practicum.filmorate.data.model.constant.EventType;
import ru.yandex.practicum.filmorate.data.model.constant.Operation;
import ru.yandex.practicum.filmorate.data.repository.FilmRepository;
import ru.yandex.practicum.filmorate.data.repository.ReviewRepository;
import ru.yandex.practicum.filmorate.data.repository.UserRepository;

import java.util.List;
import java.util.Optional;

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
        Review review = mapper.toEntity(reviewDto);
        validate(review);
        review = repository.insert(review);
        eventLogService.add(reviewDto.getUserId(), review.getId(), EventType.REVIEW, Operation.ADD);
        log.info("Создание отзыва (стоп) review: {}", review);
        return mapper.toDto(review);
    }

    public ReviewDto update(@Valid ReviewDto reviewDto) throws ConditionsException, NotFoundException {
        log.info("Редактирование отзыва (старт) review: {}", reviewDto);
        Review review = mapper.mapUpdate(repository.findByIdOrThrow(reviewDto.getReviewId()), reviewDto);
        validate(review);
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
//        var review = repository.findById(id).orElse(null);
//        if (review == null) return null;

        log.info("Получение данных отзыва (стоп) id = {}", id);
        return mapper.toDto(review);
    }

    public List<ReviewDto> getAll(Long filmId, Long count) {
        log.info("Запрос всех отзывов по фильму id = {}, количеством {}", filmId, count);
        return repository.getAll(filmId, count).stream().map(mapper::toDto).toList();
    }

    public void validate(Review review) throws NotFoundException, ConditionsException {
        Long userId = review.getUserId();
        Long filmId = review.getFilmId();
        if (userId == null) {
            throw new ConditionsException("Юзер должен быть указан");
        }
        if (filmId == null) {
            throw new ConditionsException("Фильм должен быть указан");
        }
        repositoryUser.findByIdOrThrow(userId);
        repositoryFilm.findByIdOrThrow(filmId);
        if (review.getIsPositive() == null) {
            throw new ConditionsException("Тип отзыва должен быть либо позитивный, либо негативный");
        }
        if (review.getUseful() == null) {
            review.setUseful(0);
        }
    }

    public ReviewDto addLike(Long reviewId, Long userId, boolean isDislike) throws NotFoundException, ConditionsException {
        log.info("Добавление реакции на отзыв (старт) id = {}, юзер = {}, dislike = {}", reviewId, userId, isDislike);
        repositoryUser.findByIdOrThrow(userId);
        repository.findByIdOrThrow(reviewId);
        repository.addLike(reviewId, userId, isDislike);
        //eventLogService.add(userId, reviewId, EventType.LIKE, Operation.ADD);
        Review review = repository.findByIdOrThrow(reviewId);
        log.info("Добавлена реакция isDislike = {}, useful теперь {}", isDislike, review.getUseful());
        return mapper.toDto(review); //обновление useful
    }

    public void deleteLike(Long reviewId, Long userId) throws NotFoundException, ConditionsException {
        log.info("Удаление реакции на отзыв (старт) id = {}, юзер = {}", reviewId, userId);
        repositoryUser.findByIdOrThrow(userId);
        repository.findByIdOrThrow(reviewId);
        Optional<Boolean> optIsDislike = repository.getIsDislike(reviewId, userId);
        if (optIsDislike.isEmpty()) {
            log.info("Не найдена реакция на отзыв {} юзера {}", reviewId, userId);
            return;
        }

        repository.deleteLike(reviewId, userId, optIsDislike.get());
        log.info("Удаление реакции на отзыв (стоп) id = {}, юзер = {}", reviewId, userId);
    }
}
