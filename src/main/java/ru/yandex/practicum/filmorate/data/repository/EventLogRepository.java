package ru.yandex.practicum.filmorate.data.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.data.exception.ConditionsException;
import ru.yandex.practicum.filmorate.data.exception.NotFoundException;
import ru.yandex.practicum.filmorate.data.model.EventLog;
import ru.yandex.practicum.filmorate.data.model.constant.EventType;
import ru.yandex.practicum.filmorate.data.model.constant.Operation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class EventLogRepository extends BaseRepository<EventLog> {
    private static final String INSERT_SQL = "INSERT INTO EVENT_LOG (USER_ID, EVENT_TYPE, OPERATION, ENTITY_ID) VALUES ( ?, ?, ?, ?)";

    private static final String FIND_ALL_SQL = "SELECT * FROM EVENT_LOG ORDER BY ID";
    private static final String FIND_BY_ID_SQL = "SELECT * FROM EVENT_LOG WHERE ID = ?";
    private static final String FIND_BY_USERID_SQL = """
            SELECT
                el.*
            FROM
                EVENT_LOG el
            WHERE
                el.USER_ID = ? -- События самого пользователя
                OR el.USER_ID IN ( --  события его друзей
                    SELECT
                        f.FRIEND_ID
                    FROM
                        FRIENDSHIP f
                    WHERE
                        f.USER_ID = ?
                )
            ORDER BY
                el.EVENT_DATE
            LIMIT ?
            """;

    private final JdbcTemplate jdbcTemplate;

    public EventLogRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<EventLog> findAll() {
        return findAll(FIND_ALL_SQL, this::mapToEventLog);
    }

    public List<EventLog> getFeedByUserId(Long userId, Long limit) {
        return jdbcTemplate.query(FIND_BY_USERID_SQL, (rs, rowNum) -> mapToEventLog(rs), userId, userId, limit);
    }

    public EventLog findByIdOrThrow(Long id) throws NotFoundException {
        return findByIdOrThrow(FIND_BY_ID_SQL, id, this::mapToEventLog);
    }

    public EventLog insert(EventLog eventLog) throws ConditionsException {
        return insert(INSERT_SQL, (ps, e) -> {
            ps.setLong(1, e.getUserId());
            ps.setString(2, e.getEventType().name());
            ps.setString(3, e.getOperation().name());
            ps.setLong(4, e.getEntityId());
        }, eventLog);
    }

    public EventLog insert(Long userId, Long entityId, EventType eventType, Operation operation) throws ConditionsException {
        var eventLog = EventLog.builder()
                .userId(userId)
                .eventType(eventType)
                .operation(operation)
                .entityId(entityId)
                .build();
        return this.insert(eventLog);
    }

    private EventLog mapToEventLog(ResultSet rs) throws SQLException {
        return EventLog.builder()
                .id(rs.getLong("ID"))
                .eventDate(rs.getDate("EVENT_DATE").toLocalDate())
                .userId(rs.getLong("USER_ID"))
                .eventType(EventType.valueOf(rs.getString("EVENT_TYPE")))
                .operation(Operation.valueOf(rs.getString("OPERATION")))
                .entityId(rs.getLong("ENTITY_ID"))
                .build();
    }
}