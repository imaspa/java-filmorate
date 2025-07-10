package ru.yandex.practicum.filmorate.data.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.data.exception.ConditionsException;
import ru.yandex.practicum.filmorate.data.exception.NotFoundException;
import ru.yandex.practicum.filmorate.data.model.Director;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class DirectorRepository extends BaseRepository<Director> {
    private static final String INSERT_SQL = "INSERT INTO DIRECTOR (name) VALUES (?)";
    private static final String FIND_ALL = "SELECT * FROM DIRECTOR";
    private static final String GET_DIRECTOR_BY_ID = "SELECT * FROM DIRECTOR WHERE ID =?";
    private static final String UPDATE_DIRECTOR = "UPDATE DIRECTOR SET name = ? WHERE ID = ?";
    private static final String DELETE_DIRECTOR = "DELETE FROM DIRECTOR WHERE ID = ?";
    private final JdbcTemplate jdbcTemplate;

    public DirectorRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
        this.jdbcTemplate = jdbcTemplate;
    }

    public Director insert(Director director) throws ConditionsException {
        Director savedDirector = insert(INSERT_SQL, (ps, f) -> {
            ps.setString(1, f.getName());
        }, director);
        return savedDirector;
    }

    public List<Director> getAll() {
        return jdbcTemplate.query(FIND_ALL, this::mapResultSetToDirector);
    }

    private Director mapResultSetToDirector(ResultSet resultSet, int i) throws SQLException {
        return mapToDirector(resultSet);
    }

    private Director mapToDirector(ResultSet resultSet) throws SQLException {
        return Director.builder()
                .id(resultSet.getLong("ID"))
                .name(resultSet.getString("NAME"))
                .build();
    }

    public Director getDirectorById(Long id) throws NotFoundException {
        return findByIdOrThrow(GET_DIRECTOR_BY_ID, id, this::mapToDirector);
    }

    public Director update(Director director) throws ConditionsException {
        Director savedDirector = update(UPDATE_DIRECTOR, (ps, f) -> {
            ps.setString(1, f.getName());
            ps.setLong(2, f.getId());
        }, director);
        return savedDirector;
    }

    public int deleteById(Long id) {
        return deleteById(DELETE_DIRECTOR, id);
    }
}
