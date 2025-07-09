package ru.yandex.practicum.filmorate.data.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.data.dto.ReviewDto;
import ru.yandex.practicum.filmorate.data.exception.ConditionsException;
import ru.yandex.practicum.filmorate.data.service.ReviewService;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService service;

    @PostMapping
    public ReviewDto add(@RequestBody ReviewDto reviewDto) throws ConditionsException {
        return service.add(reviewDto);
    }
}
