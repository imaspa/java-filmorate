package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.filmorate.data.exception.ConditionsException;
import ru.yandex.practicum.filmorate.data.exception.NotFoundException;
import ru.yandex.practicum.filmorate.data.model.Director;
import ru.yandex.practicum.filmorate.data.repository.DirectorRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(DirectorRepository.class)
public class DirectorRepositoryT {
    @Autowired
    private DirectorRepository directorRepository;

    @Test
    public void shouldCreateAndFindDirector() throws ConditionsException, NotFoundException {
        Director director = Director.builder().name("Режиссёр").build();
        Director savedDirector = directorRepository.insert(director);
        Director foundDirector = directorRepository.getDirectorById(savedDirector.getId());
        assertEquals(savedDirector.getId(), foundDirector.getId(), "Не удалось создать режиссёра");
    }

    @Test
    public void shouldNotFoundExceptionWhenUnknownId() {
        assertThrows(NotFoundException.class, () -> {
            directorRepository.getDirectorById(-1L);
        }, "Не работает проверка на несуществующий id режиссёра");
    }
}
