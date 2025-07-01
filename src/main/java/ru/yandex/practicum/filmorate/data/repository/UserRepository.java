package ru.yandex.practicum.filmorate.data.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.data.exception.ConditionsException;
import ru.yandex.practicum.filmorate.data.exception.NotFoundException;
import ru.yandex.practicum.filmorate.data.model.User;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository extends BaseRepository<User> {
    private static final String INSERT_SQL = "INSERT INTO USERS (NAME, LOGIN, EMAIL, BIRTHDAY) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_SQL = "UPDATE USERS SET NAME = ?, LOGIN = ?, EMAIL = ?, BIRTHDAY = ? WHERE ID = ?";
    private static final String FIND_BY_ID_SQL = "SELECT * FROM USERS WHERE ID = ?";
    private static final String FIND_ALL_SQL = "SELECT * FROM USERS";
    private static final String DELETE_SQL = "DELETE FROM USERS WHERE ID = ?";

    // друзья
    private static final String ADD_FRIEND_SQL = "INSERT INTO FRIENDSHIP (USER_ID, FRIEND_ID, ISFRIEND) VALUES (?, ?, ?)";
    private static final String REMOVE_FRIEND_SQL = "DELETE FROM FRIENDSHIP WHERE USER_ID = ? AND FRIEND_ID = ?";
    private static final String GET_FRIENDS_SQL = "SELECT u.* FROM USERS u JOIN FRIENDSHIP f ON u.ID = f.FRIEND_ID WHERE f.USER_ID = ?";
    private static final String GET_COMMON_FRIENDS_SQL = "SELECT u.* FROM USERS u JOIN FRIENDSHIP f1 ON u.ID = f1.FRIEND_ID " +
            "JOIN FRIENDSHIP f2 ON u.ID = f2.FRIEND_ID WHERE f1.USER_ID = ? AND f2.USER_ID = ?";


    public UserRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    public User insert(User user) throws ConditionsException {
        return insert(INSERT_SQL, (ps, u) -> {
            ps.setString(1, u.getName());
            ps.setString(2, u.getLogin());
            ps.setString(3, u.getEmail());
            ps.setDate(4, Date.valueOf(u.getBirthday()));
        }, user);
    }

    public User update(User user) throws ConditionsException {
        return update(UPDATE_SQL, (ps, u) -> {
            ps.setString(1, u.getName());
            ps.setString(2, u.getLogin());
            ps.setString(3, u.getEmail());
            ps.setDate(4, Date.valueOf(u.getBirthday()));
            ps.setLong(5, u.getId());
        }, user);
    }

    public Optional<User> findById(Long id) {
        return findById(FIND_BY_ID_SQL, id, this::mapToUser);
    }

    public List<User> findAll() {
        return findAll(FIND_ALL_SQL, this::mapToUser);
    }

    public int deleteById(Long id) {
        return deleteById(DELETE_SQL, id);
    }

    public User findByIdOrThrow(Long id) throws NotFoundException {
        return findByIdOrThrow(FIND_BY_ID_SQL, id, this::mapToUser);
    }

    //-- Друзья
    public void addFriend(Long userId, Long friendId, Boolean isFriend) {
        jdbcTemplate.update(ADD_FRIEND_SQL, userId, friendId, isFriend);
    }

    public void removeFriend(Long userId, Long friendId) {
        jdbcTemplate.update(REMOVE_FRIEND_SQL, userId, friendId);
    }

    public List<User> getFriends(Long userId) {
        return jdbcTemplate.query(GET_FRIENDS_SQL, (rs, rowNum) -> mapToUser(rs), userId);
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        return jdbcTemplate.query(GET_COMMON_FRIENDS_SQL, (rs, rowNum) -> mapToUser(rs), userId, otherId);
    }

    private User mapToUser(ResultSet rs) throws SQLException {
        return User.builder()
                .id(rs.getLong("ID"))
                .name(rs.getString("NAME"))
                .login(rs.getString("LOGIN"))
                .email(rs.getString("EMAIL"))
                .birthday(rs.getDate("BIRTHDAY").toLocalDate())
                .build();
    }
}