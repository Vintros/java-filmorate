package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ExistsException;
import ru.yandex.practicum.filmorate.exception.UnnownUserException;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final Map<Long, User> users = new HashMap<>();
    private long id = 0;

    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        if (users.containsKey(user.getId())) {
            throw new ExistsException("Пользователь с такой почтой уже зарегистрирован");
        }
        checkPresenceUserName(user);
        user.setId(createId());
        users.put(user.getId(), user);
        log.info("Добавлен пользователь " + user);
        return user;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        if (!users.containsKey(user.getId())) {
            throw new UnnownUserException("Пользователь не существует");
        }
        checkPresenceUserName(user);
        users.put(user.getId(), user);
        log.info("Обновлен пользователь " + user);
        return user;
    }

    @GetMapping
    public List<User> getUsers() {
        log.info("Запрошен список всех пользователей");
        return new ArrayList<>(users.values());
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
