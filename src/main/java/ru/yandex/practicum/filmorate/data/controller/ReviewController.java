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

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @GetMapping("/{id}")
    public ReviewDto getById(@PathVariable Long id) throws NotFoundException {
        return service.getById(id);
    }

    @GetMapping()
    public Collection<ReviewDto> getAll(
            @RequestParam(required = false) Long filmId,
            @RequestParam(required = false, defaultValue = "10") Long count) {
        return service.getAll(filmId, count);
    }

    @PutMapping("/{id}/like/{userid}")
    public ReviewDto addLike(@PathVariable Long id, @PathVariable Long userid) throws NotFoundException {
        return service.addLike(id, userid, false);
    }

    @PutMapping("/{id}/dislike/{userid}")
    public ReviewDto addDislike(@PathVariable Long id, @PathVariable Long userid) throws NotFoundException {
        return service.addLike(id, userid, true);
    }

    @DeleteMapping("/{id}/like/{userid}")
    public void deleteLike(@PathVariable Long id, @PathVariable Long userid) throws NotFoundException {
        service.deleteLike(id, userid);
    }

    @DeleteMapping("/{id}/dislike/{userid}")
    public void deleteDislike(@PathVariable Long id, @PathVariable Long userid) throws NotFoundException {
        service.deleteLike(id, userid);
    }

}
