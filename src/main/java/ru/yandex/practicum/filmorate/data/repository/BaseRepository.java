package ru.yandex.practicum.filmorate.data.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import ru.yandex.practicum.filmorate.data.exception.ConditionsException;
import ru.yandex.practicum.filmorate.data.exception.NotFoundException;
import ru.yandex.practicum.filmorate.data.model.Identifiable;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

public abstract class BaseRepository<T extends Identifiable> {
    protected final JdbcTemplate jdbcTemplate;

    protected BaseRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    protected T insert(String sql, PreparedStatementAction<T> action, T entity) throws ConditionsException {
        if (entity.getId() != null) {
            throw new ConditionsException("При создании записи запрещена передача идентификатора");
        }
        return insertOrUpdate(sql, action, entity, true);
    }

    protected T update(String sql, PreparedStatementAction<T> action, T entity) throws ConditionsException {
        if (entity.getId() == null) {
            throw new ConditionsException("При обновлении записи идентификатор должен быть указан");
        }
        return insertOrUpdate(sql, action, entity, false);
    }

    protected T insertOrUpdate(String sql, PreparedStatementAction<T> action, T entity, boolean isInsert) {
        if (isInsert) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                action.setValues(ps, entity);
                return ps;
            }, keyHolder);
            if (keyHolder.getKey() != null) {
                entity.setId(keyHolder.getKey().longValue());
            }
        } else {
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql);
                action.setValues(ps, entity);
                return ps;
            });
        }
        return entity;
    }

    protected Optional<T> findById(String sql, Long id, RowMapper<T> mapper) {
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapper.map(rs), id).stream().findFirst();
    }

    protected T findByIdOrThrow(String sql, Long id, RowMapper<T> mapper) throws NotFoundException {
        if (id == null) {
            throw new NotFoundException("ID не может быть null");
        }
        return findById(sql, id, mapper)
                .orElseThrow(() -> new NotFoundException("Запись с ID " + id + " не найдена"));
    }

    protected int deleteById(String sql, Long id) {
        return jdbcTemplate.update(sql, id);
    }

    protected List<T> findAll(String sql, RowMapper<T> mapper) {
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapper.map(rs));
    }

    @FunctionalInterface
    protected interface PreparedStatementAction<T> {
        void setValues(PreparedStatement ps, T entity) throws java.sql.SQLException;
    }

    @FunctionalInterface
    protected interface RowMapper<T> {
        T map(java.sql.ResultSet rs) throws java.sql.SQLException;
    }
}