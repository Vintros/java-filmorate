package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ExistsException;
import ru.yandex.practicum.filmorate.exception.UnknownUserException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.feed.FeedStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static ru.yandex.practicum.filmorate.validator.Validator.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final GenreStorage genreStorage;
    private final DirectorStorage directorStorage;
    private final FeedStorage feedStorage;

    public void addLikeFilm(Long id, Long userId) {
        validateFilm(id);
        validateUser(userId);
        Film film = filmStorage.getFilmById(id);
        if (film.getUsersIdLiked().contains(userId)) {
            throw new ExistsException(String.format("Пользователь с id: %d уже поставил лайк фильму с id: %d",
                    userId, id));
        }
        filmStorage.addLikeFilm(id, userId);
        feedStorage.saveUserEvent(new Event(userId, id, "LIKE", "ADD", new Date()));
        log.debug("Пользователь с id: {} поставил лайк фильму с id: {}", userId, id);
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
        feedStorage.saveUserEvent(new Event(userId, id, "LIKE", "REMOVE", new Date()));
        log.debug("Пользователь с id: {} удалил лайк фильма с id: {}", userId, id);
    }

    public List<Film> getListPopularFilm(Integer count, Integer genreId, Integer year) {
        validateGenreAndYear(genreId, year);
        List<Film> films;
        if (genreId != null && year != null) {
            log.info("Запрошено {} популярных фильмов по жанру №{} и {} году", count, genreId, year);
            films = filmStorage.findPopularFilmSortedByGenreAndYear(count, genreId, year);
        } else if (genreId != null) {
            log.info("Запрошено {} популярных фильмов по жанру №{}", count, genreId);
            films = filmStorage.getListPopularFilmSortedByGenre(count, genreId);
        } else if (year != null) {
            log.info("Запрошено {} популярных фильмов по {} году", count, year);
            films = filmStorage.getListPopularFilmSortedByYear(count, year);
        } else {
            log.info("Запрошено {} популярных фильмов", count);
            films = filmStorage.getListPopularFilm(count);
        }
        return addFilmsGenres(films);
    }

    private List<Film> addFilmsGenres(List<Film> films) {
        Map<Long, List<Genre>> genresByFilmsId = genreStorage.getGenresByFilmsId();
        for (Film film : films) {
            if (genresByFilmsId.get(film.getId()) != null) {
                film.getGenres().addAll(genresByFilmsId.get(film.getId()));
            }
        }
        return films;
    }

    public Film addFilm(Film film) {
        validateFilmNotExist(film);
        log.info("Фильм {} добавлен в коллекцию", film.getName());
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        validateFilm(film.getId());
        log.info("Фильм {} обновлен", film.getName());
        return filmStorage.updateFilm(film);
    }

    public List<Film> getFilms() {
        log.info("Запрошен список всех фильмов");
        List<Film> films = filmStorage.getFilmsWithoutGenres();
        return populateFilmsWithGenresAndDirectors(films);
    }

    public Film getFilmById(Long id) {
        validateFilm(id);
        log.info("Фильм с id: {}, запрошен", id);
        return filmStorage.getFilmById(id);
    }

    public void removeFilmById(Long id) {
        validateFilm(id);
        log.info("Фильм с id: {}, удалён из коллекции", id);
        filmStorage.removeFilmById(id);
    }

    public List<Film> getFilmsByDirector(Long directorId, String sortBy) {
        validateDirector(directorId);
        return filmStorage.getFilmsByDirector(directorId, sortBy);
    }

    public List<Film> searchFilmsByTitleOrDirector(String query, String searchBy) {
        log.info("Получен поисковый запрос: {}. Параметр поиска: {}", query, searchBy);
        validateSearchParameter(searchBy);
        List<Film> films;
        switch (searchBy) {
            case "title":
                films = filmStorage.searchFilmsWithoutGenresAndDirectorsByTitle(query);
                break;
            case "director":
                films = filmStorage.searchFilmsWithoutGenresAndDirectorsByDirector(query);
                break;
            case "title,director":
            case "director,title":
                films = filmStorage.searchFilmsWithoutGenresAndDirectorsByTitleAndDirector(query);
                break;
            default:
                films = new ArrayList<>();
        }
        return populateFilmsWithGenresAndDirectors(films);
    }

    public List<Film> getCommonFilms(Long userId, Long friendId) {
        validateUser(userId);
        validateUser(friendId);
        log.info("Запрошен список общих фильмов пользователей с id: {} и {}", userId, friendId);
        List<Film> films = filmStorage.getCommonFilms(userId, friendId);
        return populateFilmsWithGenresAndDirectors(films);
    }

    private List<Film> populateFilmsWithGenresAndDirectors(List<Film> films) {
        Map<Long, List<Genre>> genresByFilmsId = genreStorage.getGenresByFilmsId();
        Map<Long, List<Director>> directorsByFilmsId = directorStorage.getDirectorsByFilmsId();
        for (Film film : films) {
            if (genresByFilmsId.get(film.getId()) != null) {
                film.getGenres().addAll(genresByFilmsId.get(film.getId()));
            }
            if (directorsByFilmsId.get(film.getId()) != null) {
                film.getDirectors().addAll(directorsByFilmsId.get(film.getId()));
            }
        }
        return films;
    }
}
