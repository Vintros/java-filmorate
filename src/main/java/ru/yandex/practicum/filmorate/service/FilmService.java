package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ExistsException;
import ru.yandex.practicum.filmorate.exception.UnknownUserException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.validator.Validator;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {

    private final FilmStorage filmStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public void addLikeFilm(Long id, Long userId) {
        Validator.validateFilm(id);
        Validator.validateUser(userId);
        Film film = filmStorage.getFilmById(id);
        if (film.getUserIdLiked().contains(userId)) {
            throw new ExistsException(String.format("Пользователь с id: %d уже поставил лайк фильму с id: %d",
                    userId, id));
        }
        film.getUserIdLiked().add(userId);
        log.debug(String.format("Пользователь с id: %d поставил лайк фильму с id: %d", userId, id));
    }

    public void removeLikeFilm(Long id, Long userId) {
        Validator.validateFilm(id);
        Validator.validateUser(userId);
        Film film = filmStorage.getFilmById(id);
        Set<Long> userIdLiked = film.getUserIdLiked();
        if (!userIdLiked.contains(userId)) {
            throw new UnknownUserException(String.format("Пользователь с id: %d не ставил лайк фильму с id: %d",
                    userId, id));
        }
        log.debug(String.format("Пользователь с id: %d удалил лайк фильма с id: %d", userId, id));
        film.getUserIdLiked().remove(userId);
    }

    public List<Film> getMostLikedFilms(Integer count) {
        log.info(String.format("Запрошено %d популярных фильмов", count));
        return filmStorage.getFilms().stream()
                .sorted((o1, o2) -> o2.getUserIdLiked().size() - o1.getUserIdLiked().size())
                .limit(count)
                .collect(Collectors.toList());
    }
}
