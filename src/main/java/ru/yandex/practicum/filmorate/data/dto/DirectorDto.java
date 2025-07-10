package ru.yandex.practicum.filmorate.data.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DirectorDto {
    private Long id;
    @NotBlank(message = "Имя не может быть пустым")
    private String name;
}
