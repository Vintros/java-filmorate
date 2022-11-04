package ru.yandex.practicum.filmorate.validator;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.sql.Date;

@Service
public class Validator {

    private static final Date MOVIE_BIRTHDAY = Date.valueOf(LocalDate.of(1895, 12, 28));
    private static FilmStorage filmStorage;
    private static UserStorage userStorage;
    private static ReviewStorage reviewStorage;

    public Validator(FilmStorage filmStorage, UserStorage userStorage, ReviewStorage reviewStorage) {
        Validator.filmStorage = filmStorage;
        Validator.userStorage = userStorage;
        Validator.reviewStorage = reviewStorage;
    }

    public static void validateUser(Long id) {
        try {
            userStorage.getUserById(id);
        } catch (DataAccessException e) {
            throw new UnknownUserException(String.format("Пользователь с id: %d не найден", id));
        }
    }

    public static void validateUserNotExist(User user) {
        if (user.getId() != null && userStorage.getUserById(user.getId()) != null) {
            throw new ExistsException("Пользователь уже зарегистрирован");
        }
    }

    public static void validateFilm(Long id) {
        try {
            filmStorage.getFilmById(id);
        } catch (DataAccessException e) {
            throw new UnknownFilmException(String.format("Фильм с id: %d не найден", id));
        }
    }

    public static void validateFilmDate(Film film) {
        if (film.getReleaseDate().before(MOVIE_BIRTHDAY)) {
            throw new ValidationException("Ошибка валидации, дата релиза раньше 28 декабря 1895 года");
        }
    }

    public static void validateFilmNotExist(Film film) {
        if (film.getId() != null && filmStorage.getFilmById(film.getId()) != null) {
            throw new ExistsException("Фильм уже зарегистрирован");
        }
    }

    public static void validateGenreId(Long id) {
        if (id < 1 || id > 6) {
            throw new UnknownGenreException("Такой жанр не существует");
        }
    }

    public static void validateMpaId(Long id) {
        if (id < 1 || id > 5) {
            throw new UnknownMpaException("Такая категория не существует");
        }
    }

    public static void validateReview(Long id) {
        try {
            reviewStorage.getReviewById(id);
        } catch (DataAccessException e) {
            throw new UnknownReviewException(String.format("Отзыв с id: %d не найден", id));
        }
    }
}
