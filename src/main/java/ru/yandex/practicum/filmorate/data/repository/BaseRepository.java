package ru.yandex.practicum.filmorate.data.repository;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.data.exception.NotFoundException;
import ru.yandex.practicum.filmorate.data.model.Identifiable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class BaseRepository<T extends Identifiable> {
    final Map<Long, T> storage = new HashMap<>();

    public T addOrUpdate(T entity) {
        storage.put(entity.getId(), entity);
        return entity;
    }

    public List<T> findAll() {
        return new ArrayList<>(storage.values());
    }

    public T findById(Long id) throws NotFoundException {
        if ((id == null) || (!existsById(id))) {
            throw new NotFoundException("Запись не найдена");
        }
        return storage.get(id);
    }

    public Boolean checkExist(Long id) {
        return id != null && storage.containsKey(id);
    }

    public void deleteById(Long id) {
        storage.remove(id);
    }

    public boolean existsById(Long id) {
        return storage.containsKey(id);
    }

    public Long getNextId() {
        long currentMaxId = storage.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}

