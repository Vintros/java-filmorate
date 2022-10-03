package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ExistsException;
import ru.yandex.practicum.filmorate.exception.UnknownUserException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage{

    private final Map<Long, User> users = new HashMap<>();
    private long id = 0;

    public User createUser(User user) {
        if (users.containsKey(user.getId())) {
            throw new ExistsException("Пользователь с такой почтой уже зарегистрирован");
        }
        checkPresenceUserName(user);
        user.setId(createId());
        users.put(user.getId(), user);
        log.info("Добавлен пользователь " + user);
        return user;
    }

    public User updateUser(User user) {
        if (!users.containsKey(user.getId())) {
            throw new UnknownUserException("Пользователь не существует");
        }
        checkPresenceUserName(user);
        users.put(user.getId(), user);
        log.info("Обновлен пользователь " + user);
        return user;
    }

    public List<User> getUsers() {
        log.info("Запрошен список всех пользователей");
        return new ArrayList<>(users.values());
    }

    @Override
    public User getUserById(Long id) {
        if (!users.containsKey(id)) {
            throw new UnknownUserException("Пользователь не существует");
        }
        log.info("Запрошен пользователь с id: {}", id);
        return users.get(id);
    }

    private long createId() {
        return ++id;
    }

    private void checkPresenceUserName(User user) {
        if (user.getName() == null || user.getName().isEmpty() || user.getName().isBlank()) {
            log.debug("Имя пользователя {} пустое, в качестве имени пользователя присвоен логин", user);
            user.setName(user.getLogin());
        }
    }
}
