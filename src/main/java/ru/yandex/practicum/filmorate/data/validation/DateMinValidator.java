package ru.yandex.practicum.filmorate.data.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateMinValidator implements ConstraintValidator<DateMin, LocalDate> {
    static final String FORMATE_DATE = "yyyy-MM-dd";

    private LocalDate minDate;
    private String inputDate;
    private String message;

    @Override
    public void initialize(DateMin constraintAnnotation) {
        this.inputDate = constraintAnnotation.value();
        this.message = constraintAnnotation.message();

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(FORMATE_DATE);
            this.minDate = LocalDate.parse(inputDate, formatter);
        } catch (DateTimeParseException e) {
            // обработка в isValid
        }
    }

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        if (minDate == null) {
            throw new IllegalArgumentException("Некорректная минимальная дата '%s' для формата '%s'".formatted(inputDate, FORMATE_DATE));
        }
        if (value == null) {
            return true;
        }
        if (value.isBefore(minDate)) {
            context.disableDefaultConstraintViolation();
            String formattedMessage = message.replace("{value}", minDate.format(DateTimeFormatter.ISO_DATE));
            context.buildConstraintViolationWithTemplate(formattedMessage)
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}