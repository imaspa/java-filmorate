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

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(UserRepository.class)
class UserRepositoryIT {

    @Autowired
    private UserRepository userRepository;

    private User testUser1;
    private User testUser2;

    @BeforeEach
    void setUp() {
        testUser1 = User.builder()
                .name("Test User 1")
                .login("testlogin1")
                .email("test1@example.com")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        testUser2 = User.builder()
                .name("Test User 2")
                .login("testlogin2")
                .email("test2@example.com")
                .birthday(LocalDate.of(1995, 5, 5))
                .build();
    }

    @Test
    void shouldInsertAndFindUser() throws ConditionsException {
        User createdUser = userRepository.insert(testUser1);
        assertThat(createdUser.getId()).isNotNull();

        Optional<User> foundUser = userRepository.findById(createdUser.getId());
        assertThat(foundUser)
                .isPresent()
                .hasValueSatisfying(user -> {
                    assertThat(user.getName()).isEqualTo("Test User 1");
                    assertThat(user.getLogin()).isEqualTo("testlogin1");
                });
    }

    @Test
    void shouldUpdateUser() throws ConditionsException, NotFoundException {
        User createdUser = userRepository.insert(testUser1);
        User updatedUser = createdUser.toBuilder()
                .name("Updated Name")
                .login("updatedlogin")
                .build();

        userRepository.update(updatedUser);
        User userAfterUpdate = userRepository.findByIdOrThrow(updatedUser.getId());

        assertThat(userAfterUpdate.getName()).isEqualTo("Updated Name");
        assertThat(userAfterUpdate.getLogin()).isEqualTo("updatedlogin");
    }

    @Test
    void shouldDeleteUser() throws ConditionsException {
        User createdUser = userRepository.insert(testUser1);
        int deleteCount = userRepository.deleteById(createdUser.getId());

        assertThat(deleteCount).isEqualTo(1);
        assertThat(userRepository.findById(createdUser.getId())).isEmpty();
    }

    @Test
    void shouldFindAllUsers() throws ConditionsException {
        userRepository.insert(testUser1);
        userRepository.insert(testUser2);

        List<User> users = userRepository.findAll();
        assertThat(users)
                .extracting(User::getId)
                .hasSize(2);
    }

    @Test
    void shouldManageFriends() throws ConditionsException, NotFoundException {
        User user1 = userRepository.insert(testUser1);
        User user2 = userRepository.insert(testUser2);

        // Добавляем друга
        userRepository.addFriend(user1.getId(), user2.getId(), true);

        // Проверяем список друзей
        List<User> friends = userRepository.getFriends(user1.getId());
        assertThat(friends)
                .hasSize(1)
                .extracting(User::getId)
                .containsExactly(user2.getId());

        // Создаем третьего пользователя для проверки общих друзей
        User user3 = userRepository.insert(
                User.builder()
                        .name("User 3")
                        .login("user3")
                        .email("user3@example.com")
                        .birthday(LocalDate.of(2000, 10, 10))
                        .build()
        );

        // Добавляем того же друга второму пользователю
        userRepository.addFriend(user3.getId(), user2.getId(), true);

        // Проверяем общих друзей
        List<User> commonFriends = userRepository.getCommonFriends(user1.getId(), user3.getId());
        assertThat(commonFriends)
                .hasSize(1)
                .extracting(User::getId)
                .containsExactly(user2.getId());

        // Удаляем друга
        userRepository.removeFriend(user1.getId(), user2.getId());

        // Проверяем что друг удален
        assertThat(userRepository.getFriends(user1.getId())).isEmpty();
    }
}