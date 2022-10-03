package ru.yandex.practicum.filmorate.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.UnknownFilmException;
import ru.yandex.practicum.filmorate.exception.UnknownUserException;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

@Service
public class Validator {

    private static FilmStorage filmStorage;
    private static UserStorage userStorage;

    @Autowired
    public Validator(FilmStorage filmStorage, UserStorage userStorage) {
        Validator.filmStorage = filmStorage;
        Validator.userStorage = userStorage;
    }

    public static void validateFilm(Long id) {
        if (filmStorage.getFilmById(id) == null) {
            throw new UnknownFilmException(String.format("Фильм с id: %d не найден", id));
        }
    }

    public static void validateUser(Long id) {
        if (userStorage.getUserById(id) == null) {
            throw new UnknownUserException(String.format("Пользователь с id: %d не найден", id));
        }
    }
}
