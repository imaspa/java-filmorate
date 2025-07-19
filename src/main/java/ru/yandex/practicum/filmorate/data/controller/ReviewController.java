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
import ru.yandex.practicum.filmorate.data.dto.ReviewDto;
import ru.yandex.practicum.filmorate.data.exception.ConditionsException;
import ru.yandex.practicum.filmorate.data.exception.NotFoundException;
import ru.yandex.practicum.filmorate.data.service.ReviewService;

import java.util.Collection;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService service;

    @PostMapping
    public ReviewDto add(@RequestBody ReviewDto reviewDto) throws ConditionsException, NotFoundException {
        return service.add(reviewDto);
    }

    @PutMapping
    public ReviewDto update(@RequestBody ReviewDto reviewDto) throws ConditionsException, NotFoundException {
        return service.update(reviewDto);
    }

    @DeleteMapping("/{reviewId}")
    public void delete(@PathVariable Long reviewId) throws ConditionsException, NotFoundException {
        service.delete(reviewId);
    }

    @GetMapping("/{reviewId}")
    public ReviewDto getById(@PathVariable Long reviewId) throws NotFoundException {
        return service.getById(reviewId);
    }

    @GetMapping()
    public Collection<ReviewDto> getAll(
            @RequestParam(required = false) Long filmId,
            @RequestParam(required = false, defaultValue = "10") Long count) {
        return service.getAll(filmId, count);
    }

    @PutMapping("/{reviewId}/like/{userId}")
    public ReviewDto addLike(@PathVariable Long reviewId, @PathVariable Long userId) throws NotFoundException, ConditionsException {
        return service.addLike(reviewId, userId, false);
    }

    @PutMapping("/{reviewId}/dislike/{userId}")
    public ReviewDto addDislike(@PathVariable Long reviewId, @PathVariable Long userId) throws NotFoundException, ConditionsException {
        return service.addLike(reviewId, userId, true);
    }

    @DeleteMapping("/{reviewId}/like/{userId}")
    public void deleteLike(@PathVariable Long reviewId, @PathVariable Long userId) throws NotFoundException, ConditionsException {
        service.deleteLike(reviewId, userId);
    }

    @DeleteMapping("/{reviewId}/dislike/{userId}")
    public void deleteDislike(@PathVariable Long reviewId, @PathVariable Long userId) throws NotFoundException, ConditionsException {
        service.deleteLike(reviewId, userId);
    }

}
