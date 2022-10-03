package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ExistsException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.validator.Validator;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {

    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public void addFriend(Long id, Long friendId) {
        Validator.validateUser(id);
        Validator.validateUser(friendId);
        User firstUser = userStorage.getUserById(id);
        User secondUser = userStorage.getUserById(friendId);
        if (firstUser.getFriends().contains(friendId) && secondUser.getFriends().contains(id)) {
            throw new ExistsException(String.format("Пользователи с id: %d, %d уже друзья", id, friendId));
        }
        firstUser.getFriends().add(friendId);
        secondUser.getFriends().add(id);
        log.info(String.format("Пользователь с id: %d добавил в друзья пользователя с id: %d", id, friendId));
    }

    public void deleteFriend(Long id, Long friendId) {
        Validator.validateUser(id);
        Validator.validateUser(friendId);
        User firstUser = userStorage.getUserById(id);
        User secondUser = userStorage.getUserById(friendId);
        if (!firstUser.getFriends().contains(friendId) && !secondUser.getFriends().contains(id)) {
            throw new ExistsException(String.format("Пользователи с id: %d, %d не друзья", id, friendId));
        }
        firstUser.getFriends().remove(friendId);
        secondUser.getFriends().remove(id);
        log.info(String.format("Пользователь с id: %d удалил из друзей пользователя с id: %d", id, friendId));
    }

    public List<User> getFriends(Long id) {
        Validator.validateUser(id);
        User user = userStorage.getUserById(id);
        log.info(String.format("Пользователь с id: %d запросил список друзей", id));
        return user.getFriends().stream()
                .map(userStorage::getUserById)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(Long id, Long secondId) {
        Validator.validateUser(id);
        Validator.validateUser(secondId);
        User firstUser = userStorage.getUserById(id);
        User secondUser = userStorage.getUserById(secondId);
        log.info(String.format("Пользователь с id: %d запросил список общих друзей с id: %d", id, secondId));
        return firstUser.getFriends().stream()
                .filter(friend -> secondUser.getFriends().contains(friend))
                .map(userStorage::getUserById)
                .collect(Collectors.toList());
    }
}
