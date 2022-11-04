package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ExistsException;
import ru.yandex.practicum.filmorate.exception.UnknownUserException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.*;
import java.util.stream.Collectors;

import static ru.yandex.practicum.filmorate.validator.Validator.*;

@Service
@Slf4j
public class FilmService {

    private final FilmStorage filmStorage;
    private final GenreStorage genreStorage;

    public FilmService(FilmStorage filmStorage, GenreStorage genreStorage) {
        this.filmStorage = filmStorage;
        this.genreStorage = genreStorage;
    }

    public void addLikeFilm(Long id, Long userId) {
        validateFilm(id);
        validateUser(userId);
        Film film = filmStorage.getFilmById(id);
        if (film.getUsersIdLiked().contains(userId)) {
            throw new ExistsException(String.format("Пользователь с id: %d уже поставил лайк фильму с id: %d",
                    userId, id));
        }
        filmStorage.addLikeFilm(id, userId);
        log.debug(String.format("Пользователь с id: %d поставил лайк фильму с id: %d", userId, id));
    }

    public void removeLikeFilm(Long id, Long userId) {
        validateFilm(id);
        validateUser(userId);
        try {
            filmStorage.removeLikeFilm(id, userId);
        } catch (DataAccessException e) {
            throw new UnknownUserException(String.format("Пользователь с id: %d не ставил лайк фильму с id: %d",
                    userId, id));
        }
        log.debug(String.format("Пользователь с id: %d удалил лайк фильма с id: %d", userId, id));
    }

    public List<Film> getMostLikedFilms(Integer count) {
        log.info(String.format("Запрошено %d популярных фильмов", count));
        List<Film> films = getFilms();
        Map<Long, List<Long>> usersIdLiked = filmStorage.getUsersIdLiked();
        for (Film film : films) {
            if (usersIdLiked.get(film.getId()) != null) {
                film.getUsersIdLiked().addAll(usersIdLiked.get(film.getId()));
            }
        }
        return films.stream()
                .sorted((o1, o2) -> o2.getUsersIdLiked().size() - o1.getUsersIdLiked().size())
                .limit(count)
                .collect(Collectors.toList());
    }

    public Film addFilm(Film film) {
        validateFilmNotExist(film);
        validateFilmDate(film);
        log.info("Фильм {} добавлен в коллекцию", film.getName());
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        validateFilm(film.getId());
        validateFilmDate(film);
        log.info("Фильм {} обновлен", film.getName());
        return filmStorage.updateFilm(film);
    }

    public List<Film> getFilms() {
        log.info("Запрошен список всех фильмов");
        List<Film> films = filmStorage.getFilmsWithoutGenres();
        Map<Long, List<Genre>> genresByFilmsId = genreStorage.getGenresByFilmsId();
        for (Film film : films) {
            if (genresByFilmsId.get(film.getId()) != null) {
                film.getGenres().addAll(genresByFilmsId.get(film.getId()));
            }
        }
        return films;
    }

    public Film getFilmById(Long id) {
        validateFilm(id);
        log.info("Фильм с id: {}, запрошен", id);
        return filmStorage.getFilmById(id);
    }

    public void removeFilmById(Long id) {
        validateFilm(id);
        log.info("Фильм ID_{} удалён из коллекции", id);
        filmStorage.removeFilmById(id);
    }
}
