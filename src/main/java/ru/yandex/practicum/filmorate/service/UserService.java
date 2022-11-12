package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ExistsException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.feed.FeedStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;
    private final FeedStorage feedStorage;

    public void addFriend(Long id, Long friendId) {
        userStorage.checkUserExistsById(id);
        userStorage.checkUserExistsById(friendId);
        User firstUser = userStorage.getUserById(id);
        User secondUser = userStorage.getUserById(friendId);
        if (firstUser.getFriends().contains(secondUser)) {
            throw new ExistsException(String.format(
                    "User with id: %d has already made user with id: %d his friend", id, friendId));
        }
        userStorage.addFriend(id, friendId);
        feedStorage.saveUserEvent(new Event(id, friendId, "FRIEND", "ADD",  new Date()));
        log.info(String.format("A user with id: %d has made a user with id: %d a friend", id, friendId));
    }

    public void deleteFriend(Long id, Long friendId) {
        userStorage.checkUserExistsById(id);
        userStorage.checkUserExistsById(friendId);
        User firstUser = userStorage.getUserById(id);
        User secondUser = userStorage.getUserById(friendId);
        if (!firstUser.getFriends().contains(secondUser)) {
            throw new ExistsException(String.format(
                    "A user with id: %d is not friends with user with id: %d", id, friendId));
        }
        userStorage.removeFriend(id, friendId);
        feedStorage.saveUserEvent(new Event(id, friendId, "FRIEND", "REMOVE",  new Date()));
        log.info(String.format("A user with id: %d has removed a user with id: %d from friends", id, friendId));
    }

    public List<User> getFriends(Long id) {
        userStorage.checkUserExistsById(id);
        User user = userStorage.getUserById(id);
        log.info(String.format("User with id: %d requested a list of friends", id));
        return new ArrayList<>(user.getFriends());
    }

    public List<User> getCommonFriends(Long id, Long secondId) {
        userStorage.checkUserExistsById(id);
        userStorage.checkUserExistsById(secondId);
        log.info(String.format("A user with id: %d requested a list of common friends with id: %d", id, secondId));
        return userStorage.getCommonFriends(id, secondId);
    }

    public User createUser(User user) {
        if (user.getId() != null) {
            userStorage.checkUserNotExistById(user.getId());
        }
        checkPresenceUserName(user);
        log.info("User is added: " + user);
        return userStorage.createUser(user);
    }

    public User updateUser(User user) {
        userStorage.checkUserExistsById(user.getId());
        checkPresenceUserName(user);
        log.info("User is updated: " + user);
        return userStorage.updateUser(user);
    }

    public List<User> getUsers() {
        log.info("A list of all users is requested");
        return userStorage.getUsers();
    }

    public User getUserById(Long id) {
        userStorage.checkUserExistsById(id);
        log.info("A user with id: {} is requested", id);
        return userStorage.getUserById(id);
    }

    public void removeUserById(Long id) {
        userStorage.checkUserExistsById(id);
        log.info("A user with id: {} is removed", id);
        userStorage.removeUserById(id);
    }

    public List<Event> getFeed(Long id) {
        userStorage.checkUserExistsById(id);
        log.info("Requested event feed of a user with id: {}", id);
        return feedStorage.getFeed(id);
    }

    private void checkPresenceUserName(User user) {
        if (user.getName() == null || user.getName().isEmpty() || user.getName().isBlank()) {
            log.debug("The user name {} is empty, the login is assigned as the user name", user);
            user.setName(user.getLogin());
        }
    }
}
