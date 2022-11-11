package ru.yandex.practicum.filmorate.validator;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

@Service
public class Validator {

    private static FilmStorage filmStorage;
    private static UserStorage userStorage;
    private static ReviewStorage reviewStorage;
    private static DirectorStorage directorStorage;

    public Validator(FilmStorage filmStorage,
                     UserStorage userStorage,
                     ReviewStorage reviewStorage,
                     DirectorStorage directorStorage) {
        Validator.filmStorage = filmStorage;
        Validator.userStorage = userStorage;
        Validator.reviewStorage = reviewStorage;
        Validator.directorStorage = directorStorage;
    }

    public static void validateUser(Long id) {
        try {
            userStorage.getUserById(id);
        } catch (DataAccessException e) {
            throw new UnknownUserException(String.format("User with id: %d is not found", id));
        }
    }

    public static void validateUserNotExist(User user) {
        if (user.getId() != null && userStorage.getUserById(user.getId()) != null) {
            throw new ExistsException("The user has been already registered");
        }
    }

    public static void validateFilm(Long id) {
        try {
            filmStorage.getFilmById(id);
        } catch (DataAccessException e) {
            throw new UnknownFilmException(String.format("Movie with id: %d is not found", id));
        }
    }

    public static void validateFilmNotExist(Film film) {
        if (film.getId() != null && filmStorage.getFilmById(film.getId()) != null) {
            throw new ExistsException("The movie already exists");
        }
    }

    public static void validateGenreId(Long id) {
        if (id < 1 || id > 6) {
            throw new UnknownGenreException("There is no such a genre");
        }
    }

    public static void validateMpaId(Long id) {
        if (id < 1 || id > 5) {
            throw new UnknownMpaException("There is no such a category");
        }
    }

    public static void validateReview(Long id) {
        try {
            reviewStorage.getReviewById(id);
        } catch (DataAccessException e) {
            throw new UnknownReviewException(String.format("Review with id: %d is not found", id));
        }
    }

    public static void validateDirector(Long id) {
        try {
            directorStorage.getDirectorById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new UnknownDirectorException(String.format("Director with id: %d is not found", id));
        }
    }

    public static void validateDirectorNotExist(Director director) {
        try {
            directorStorage.getDirectorById(director.getId());
            throw new ExistsException("The director already exists");
        } catch (EmptyResultDataAccessException ignored) {
        }
    }

    public static void validateGenreAndYear(Integer genreId, Integer year) {
        if (year != null && year < 1895 || genreId != null && genreId <= 0) {
            throw new RuntimeException("Incorrect request");
        }
    }

    public static void validateSearchParameter(String by) {
        if (!by.equals("title") && !by.equals("director") && !by.equals("title,director") && !by.equals("director,title")) {
            throw new IncorrectSearchParameterException("An incorrect search parameter has been entered. Available values: " +
                    "title; director, title,director; director,title");
        }
    }
}
