package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {

    User createUser(User user);

    User updateUser(User user);

    List<User> getUsers();

    User getUserById(Long id);

    void addFriend(Long id, Long friendId);

    void removeFriend(Long id, Long friendId);

    List<User> getCommonFriends(Long id, Long friendId);

    void removeUserById(Long id);

    void validateUser(Long id);

    void checkUserNotExist(User user);
}
