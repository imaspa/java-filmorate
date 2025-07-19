package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.filmorate.data.exception.ConditionsException;
import ru.yandex.practicum.filmorate.data.exception.NotFoundException;
import ru.yandex.practicum.filmorate.data.model.User;
import ru.yandex.practicum.filmorate.data.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@Import(UserRepository.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class UserRepositoryIT {

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() throws ConditionsException {
        testUser = User.builder()
                .name("Alice")
                .login("alice_login")
                .email("alice@example.com")
                .birthday(LocalDate.of(1990, 5, 15))
                .build();

        testUser = userRepository.insert(testUser);
    }

    @Test
    void shouldInsertUser() throws ConditionsException {
        User newUser = User.builder()
                .name("Bob")
                .login("bob_login")
                .email("bob@example.com")
                .birthday(LocalDate.of(1985, 7, 20))
                .build();

        User created = userRepository.insert(newUser);

        assertThat(created.getId()).isNotNull();
        Optional<User> found = userRepository.findById(created.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Bob");
    }

    @Test
    void shouldUpdateUser() throws ConditionsException {
        testUser.setName("Updated Name");
        testUser.setEmail("updated@example.com");

        User updated = userRepository.update(testUser);

        assertThat(updated.getName()).isEqualTo("Updated Name");

        Optional<User> found = userRepository.findById(testUser.getId());
        assertThat(found).isPresent()
                .get()
                .extracting(User::getEmail)
                .isEqualTo("updated@example.com");
    }

    @Test
    void shouldFindUserById() throws NotFoundException {
        User found = userRepository.findByIdOrThrow(testUser.getId());
        assertThat(found).isNotNull();
        assertThat(found.getLogin()).isEqualTo("alice_login");
    }

    @Test
    void shouldReturnEmptyForNonexistentId() {
        Optional<User> found = userRepository.findById(9999L);
        assertThat(found).isEmpty();
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUserNotFound() {
        assertThatThrownBy(() -> userRepository.findByIdOrThrow(9999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Запись с ID 9999 не найдена");
    }

    @Test
    void shouldFindAllUsers() throws ConditionsException {
        User user2 = User.builder()
                .name("Charlie")
                .login("charlie_login")
                .email("charlie@example.com")
                .birthday(LocalDate.of(1992, 3, 14))
                .build();

        userRepository.insert(user2);

        List<User> users = userRepository.findAll();
        assertThat(users)
                .hasSizeGreaterThanOrEqualTo(2)
                .extracting(User::getName)
                .contains("Alice", "Charlie");
    }

    @Test
    void shouldReturnEmptyListWhenNoFriends() {
        List<User> friends = userRepository.getFriends(testUser.getId());
        assertThat(friends).isEmpty();
    }

    @Test
    void shouldReturnEmptyCommonFriendsWhenNoneExist() throws ConditionsException {
        User anotherUser = userRepository.insert(User.builder()
                .name("David")
                .login("david_login")
                .email("david@example.com")
                .birthday(LocalDate.of(1993, 4, 18))
                .build());
        List<User> commonFriends = userRepository.getCommonFriends(testUser.getId(), anotherUser.getId());
        assertThat(commonFriends).isEmpty();
    }
}
