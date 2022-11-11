package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ExistsException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.feed.FeedStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private final FeedStorage feedStorage;

    public void addFriend(Long id, Long friendId) {
        userStorage.checkUserExists(id);
        userStorage.checkUserExists(friendId);
        User firstUser = userStorage.getUserById(id);
        User secondUser = userStorage.getUserById(friendId);
        if (firstUser.getFriends().contains(secondUser)) {
            throw new ExistsException(String.format("Пользователь с id: %d, уже дружит с %d", id, friendId));
        }
        userStorage.addFriend(id, friendId);
        feedStorage.saveUserEvent(new Event(id, friendId, "FRIEND", "ADD",  new Date()));
        log.info(String.format("Пользователь с id: %d добавил в друзья пользователя с id: %d", id, friendId));
    }

    public void deleteFriend(Long id, Long friendId) {
        userStorage.checkUserExists(id);
        userStorage.checkUserExists(friendId);
        User firstUser = userStorage.getUserById(id);
        User secondUser = userStorage.getUserById(friendId);
        if (!firstUser.getFriends().contains(secondUser)) {
            throw new ExistsException(String.format("Пользователь с id: %d не дружит с %d", id, friendId));
        }
        userStorage.removeFriend(id, friendId);
        feedStorage.saveUserEvent(new Event(id, friendId, "FRIEND", "REMOVE",  new Date()));
        log.info(String.format("Пользователь с id: %d удалил из друзей пользователя с id: %d", id, friendId));
    }

    public List<User> getFriends(Long id) {
        userStorage.checkUserExists(id);
        User user = userStorage.getUserById(id);
        log.info(String.format("Пользователь с id: %d запросил список друзей", id));
        return new ArrayList<>(user.getFriends());
    }

    public List<User> getCommonFriends(Long id, Long secondId) {
        userStorage.checkUserExists(id);
        userStorage.checkUserExists(secondId);
        log.info(String.format("Пользователь с id: %d запросил список общих друзей с id: %d", id, secondId));
        return userStorage.getCommonFriends(id, secondId);
    }

    public User createUser(User user) {
        userStorage.checkUserNotExist(user);
        checkPresenceUserName(user);
        log.info("Добавлен пользователь " + user);
        return userStorage.createUser(user);
    }

    public User updateUser(User user) {
        userStorage.checkUserExists(user.getId());
        checkPresenceUserName(user);
        log.info("Обновлен пользователь " + user);
        return userStorage.updateUser(user);
    }

    public List<User> getUsers() {
        log.info("Запрошен список всех пользователей");
        return userStorage.getUsers();
    }

    public User getUserById(Long id) {
        userStorage.checkUserExists(id);
        log.info("Запрошен пользователь с id: {}", id);
        return userStorage.getUserById(id);
    }

    public void removeUserById(Long id) {
        userStorage.checkUserExists(id);
        log.info("удалён пользователь с id: {}", id);
        userStorage.removeUserById(id);
    }

    public List<Film> getRecommendations(Long id) {
        userStorage.checkUserExists(id);
        log.info("Пользователем с id - {} запрошен список рекомендованных фильмов", id);
        return filmStorage.getRecommendations(id);
    }

    public List<Event> getFeed(Long id) {
        userStorage.checkUserExists(id);
        return feedStorage.getFeed(id);
    }

    private void checkPresenceUserName(User user) {
        if (user.getName() == null || user.getName().isEmpty() || user.getName().isBlank()) {
            log.debug("Имя пользователя {} пустое, в качестве имени пользователя присвоен логин", user);
            user.setName(user.getLogin());
        }
    }
}
