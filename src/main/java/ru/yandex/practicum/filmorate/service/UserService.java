package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ExistsException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.List;

import static ru.yandex.practicum.filmorate.validator.Validator.validateUser;
import static ru.yandex.practicum.filmorate.validator.Validator.validateUserNotExist;

@Service
@Slf4j
public class UserService {

    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public void addFriend(Long id, Long friendId) {
        validateUser(id);
        validateUser(friendId);
        User firstUser = userStorage.getUserById(id);
        User secondUser = userStorage.getUserById(friendId);
        if (firstUser.getFriends().contains(secondUser)) {
            throw new ExistsException(String.format("Пользователь с id: %d, уже дружит с %d", id, friendId));
        }
        userStorage.addFriend(id, friendId);
        log.info(String.format("Пользователь с id: %d добавил в друзья пользователя с id: %d", id, friendId));
    }

    public void deleteFriend(Long id, Long friendId) {
        validateUser(id);
        validateUser(friendId);
        User firstUser = userStorage.getUserById(id);
        User secondUser = userStorage.getUserById(friendId);
        if (!firstUser.getFriends().contains(secondUser)) {
            throw new ExistsException(String.format("Пользователь с id: %d не дружит с %d", id, friendId));
        }
        userStorage.removeFriend(id, friendId);
        log.info(String.format("Пользователь с id: %d удалил из друзей пользователя с id: %d", id, friendId));
    }

    public List<User> getFriends(Long id) {
        validateUser(id);
        User user = userStorage.getUserById(id);
        log.info(String.format("Пользователь с id: %d запросил список друзей", id));
        return new ArrayList<>(user.getFriends());
    }

    public List<User> getCommonFriends(Long id, Long secondId) {
        validateUser(id);
        validateUser(secondId);
        log.info(String.format("Пользователь с id: %d запросил список общих друзей с id: %d", id, secondId));
        return userStorage.getCommonFriends(id, secondId);
    }

    public User createUser(User user) {
        validateUserNotExist(user);
        checkPresenceUserName(user);
        log.info("Добавлен пользователь " + user);
        return userStorage.createUser(user);
    }

    public User updateUser(User user) {
        validateUser(user.getId());
        checkPresenceUserName(user);
        log.info("Обновлен пользователь " + user);
        return userStorage.updateUser(user);
    }

    public List<User> getUsers() {
        log.info("Запрошен список всех пользователей");
        return userStorage.getUsers();
    }

    public User getUserById(Long id) {
        validateUser(id);
        log.info("Запрошен пользователь с id: {}", id);
        return userStorage.getUserById(id);
    }

    private void checkPresenceUserName(User user) {
        if (user.getName() == null || user.getName().isEmpty() || user.getName().isBlank()) {
            log.debug("Имя пользователя {} пустое, в качестве имени пользователя присвоен логин", user);
            user.setName(user.getLogin());
        }
    }
}
