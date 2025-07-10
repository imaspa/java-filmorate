package ru.yandex.practicum.filmorate.data.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.data.dto.ReviewDto;
import ru.yandex.practicum.filmorate.data.exception.ConditionsException;
import ru.yandex.practicum.filmorate.data.exception.NotFoundException;
import ru.yandex.practicum.filmorate.data.mapper.ReviewMapper;
import ru.yandex.practicum.filmorate.data.model.Review;
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

    public ReviewDto add(ReviewDto reviewDto) throws ConditionsException, NotFoundException {
        log.info("Создание отзыва (старт) review: {}", reviewDto);
        Review review = mapper.toEntity(reviewDto);
        validate(review);
        review = repository.insert(review);
        log.info("Создание отзыва (стоп) review: {}", review);
        return mapper.toDto(review);
    }

    public ReviewDto update(ReviewDto reviewDto) throws ConditionsException, NotFoundException {
        log.info("Редактирование отзыва (старт) review: {}", reviewDto);
        Review review = mapper.map(repository.findByIdOrThrow(reviewDto.getReviewId()), reviewDto);
        validate(review);
        repository.update(review);
        log.info("Редактирование отзыва (стоп) review: {}", review);
        return mapper.toDto(review);
    }

    public void delete(Long id) {
        log.info("Удаление отзыва (старт) id: {}", id);
        int count = repository.deleteById(id);
        log.info("Удаление отзыва (стоп) id: {}, удалено {}", id, count);
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

    public ReviewDto addLike(Long id, Long userid, boolean isDislike) throws NotFoundException {
        log.info("Добавление реакции на отзыв (старт) id = {}, юзер = {}, dislike = {}", id, userid, isDislike);
        repositoryUser.findByIdOrThrow(userid);
        repository.findByIdOrThrow(id);
        repository.addLike(id, userid, isDislike);
        Review review = repository.findByIdOrThrow(id);
        log.info("Добавлена реакция isDislike = {}, useful теперь {}", isDislike, review.getUseful());
        return mapper.toDto(review); //обновление useful
    }

    public void deleteLike(Long id, Long userid) throws NotFoundException {
        log.info("Удаление реакции на отзыв (старт) id = {}, юзер = {}", id, userid);
        repositoryUser.findByIdOrThrow(userid);
        repository.findByIdOrThrow(id);
        Optional<Boolean> optIsDislike = repository.getIsDislike(id, userid);
        if (optIsDislike.isEmpty()) {
            log.info("Не найдена реакция на отзыв {} юзера {}", id, userid);
            return;
        }
        repository.deleteLike(id, userid, optIsDislike.get());
        log.info("Удаление реакции на отзыв (стоп) id = {}, юзер = {}", id, userid);
    }
}
