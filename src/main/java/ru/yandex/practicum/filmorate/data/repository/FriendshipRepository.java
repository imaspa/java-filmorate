package ru.yandex.practicum.filmorate.data.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.data.exception.ConditionsException;
import ru.yandex.practicum.filmorate.data.exception.NotFoundException;
import ru.yandex.practicum.filmorate.data.model.Friendship;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
public class FriendshipRepository extends BaseRepository<Friendship> {
    private static final String FIND_BY_USER_AND_FRIEND_SQL = "SELECT * FROM FRIENDSHIP WHERE USER_ID = ? AND FRIEND_ID = ? LIMIT 1";
    private static final String ADD_FRIEND_SQL = "INSERT INTO FRIENDSHIP (USER_ID, FRIEND_ID, ISFRIEND) VALUES (?, ?, ?)";
    private static final String DELETE_SQL = "DELETE FROM FRIENDSHIP WHERE ID = ?";

    protected FriendshipRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    public Friendship addFriend(Long userId, Long friendId, Boolean isFriend) throws ConditionsException {
        Friendship friendship = Friendship.builder()
                .userId(userId)
                .friendId(friendId)
                .isFriend(isFriend)
                .build();
        return addFriend(friendship);
    }

    public Friendship addFriend(Friendship friendship) throws ConditionsException {
        return insert(ADD_FRIEND_SQL, (ps, f) -> {
            ps.setLong(1, f.getUserId());
            ps.setLong(2, f.getFriendId());
            ps.setBoolean(3, f.getIsFriend());
        }, friendship);
    }

    public void removeFriend(Long id) {
        deleteById(DELETE_SQL, id);
    }

    public Friendship findByUserIdAndFriendIdOrThrow(Long userId, Long friendsId) throws NotFoundException {
        return findByUserIdAndFriendId(userId, friendsId, this::mapToFriendship)
                .orElseThrow(() -> new NotFoundException("Запись с userId: %s и friendsId: %s не найдена".formatted(userId, friendsId)));

    }

    public Optional<Friendship> findByUserIdAndFriendId(Long userId, Long friendsId) {
        return findByUserIdAndFriendId(userId, friendsId, this::mapToFriendship);
    }

    protected Optional<Friendship> findByUserIdAndFriendId(Long userId, Long friendsId, RowMapper<Friendship> mapper) {
        return jdbcTemplate.query(FIND_BY_USER_AND_FRIEND_SQL, (rs, rowNum) -> mapper.map(rs), userId, friendsId).stream().findFirst();
    }

    private Friendship mapToFriendship(ResultSet rs) throws SQLException {
        return Friendship.builder()
                .id(rs.getLong("ID"))
                .userId(rs.getLong("USER_ID"))
                .friendId(rs.getLong("FRIEND_ID"))
                .isFriend(rs.getBoolean("ISFRIEND"))
                .build();
    }


}