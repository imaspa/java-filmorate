package ru.yandex.practicum.filmorate.data.model;

import lombok.Data;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class User implements Identifiable {

    private final Set<User> friends = new HashSet<>();
    private Long id;
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;

    public Boolean isFriend(User friend) {
        return friends.contains(friend);
    }

    public void addFriend(User friend) {
        friends.add(friend);
    }

    public void removeFriend(User friend) {
        friends.remove(friend);
    }

    public Set<User> getFriends() {
        return Collections.unmodifiableSet(friends);
    }

    public Set<User> getCommonFriends(User otherUser) {
        return this.friends
                .stream()
                .filter(otherUser.getFriends()::contains)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
