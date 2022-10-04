package ru.yandex.practicum.filmorate.validator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ExistsException;
import ru.yandex.practicum.filmorate.exception.UnknownFilmException;
import ru.yandex.practicum.filmorate.exception.UnknownUserException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;

@Service
public class Validator {

    private static final LocalDate MOVIE_BIRTHDAY = LocalDate.of(1895, 12, 28);
    private static FilmStorage filmStorage;
    private static UserStorage userStorage;

    public Validator(FilmStorage filmStorage, UserStorage userStorage) {
        Validator.filmStorage = filmStorage;
        Validator.userStorage = userStorage;
    }

    public static void validateUser(Long id) {
        if (userStorage.getUserById(id) == null) {
            throw new UnknownUserException(String.format("Пользователь с id: %d не найден", id));
        }
    }

    public static void validateUserNotExist(User user) {
        if (user.getId() != null && userStorage.getUserById(user.getId()) != null) {
            throw new ExistsException("Пользователь уже зарегистрирован");
        }
    }

    public static void validateFilm(Long id) {
        if (filmStorage.getFilmById(id) == null) {
            throw new UnknownFilmException(String.format("Фильм с id: %d не найден", id));
        }
    }

    public static void validateFilmDate(Film film) {
        if (film.getReleaseDate().isBefore(MOVIE_BIRTHDAY)) {
            throw new ValidationException("Ошибка валидации, дата релиза раньше 28 декабря 1895 года");
        }
    }

    public static void validateFilmNotExist(Film film) {
        if (film.getId() != null && filmStorage.getFilmById(film.getId()) != null) {
            throw new ExistsException("Фильм уже зарегистрирован");
        }
    }
}
