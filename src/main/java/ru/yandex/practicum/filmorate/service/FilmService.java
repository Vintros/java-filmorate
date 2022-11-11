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
            throw new ExistsException(String.format("A user with id: %d has already liked a movie with id: %d",
                    userId, id));
        }
        filmStorage.addLikeFilm(id, userId);
        feedStorage.saveUserEvent(new Event(userId, id, "LIKE", "ADD", new Date()));
        log.debug("User with id: {} has liked the movie with id: {}", userId, id);
    }

    public void removeLikeFilm(Long id, Long userId) {
        validateFilm(id);
        validateUser(userId);
        try {
            filmStorage.removeLikeFilm(id, userId);
        } catch (DataAccessException e) {
            throw new UnknownUserException(String.format("The user with id: %d has not liked the movie with id: %d",
                    userId, id));
        }
        feedStorage.saveUserEvent(new Event(userId, id, "LIKE", "REMOVE", new Date()));
        log.debug("A user with id: {} removed a movie like with id: {}", userId, id);
    }

    public List<Film> getListPopularFilm(Integer count, Integer genreId, Integer year) {
        validateGenreAndYear(genreId, year);
        List<Film> films;
        if (genreId != null && year != null) {
            log.info("{} popular movies by genre #{} and {} year is/are requested", count, genreId, year);
            films = filmStorage.findPopularFilmSortedByGenreAndYear(count, genreId, year);
        } else if (genreId != null) {
            log.info("{} popular movies by genre No.{} is/are requested", count, genreId);
            films = filmStorage.getListPopularFilmSortedByGenre(count, genreId);
        } else if (year != null) {
            log.info("{} popular movies by {} year is/are requested", count, year);
            films = filmStorage.getListPopularFilmSortedByYear(count, year);
        } else {
            log.info("{} popular movies is/are requested", count);
            films = filmStorage.getListPopularFilm(count);
        }
        return addFilmsGenres(films);
    }

    public Film addFilm(Film film) {
        validateFilmNotExist(film);
        log.info("Movie {} is added to collection", film.getName());
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        validateFilm(film.getId());
        log.info("The movie {} has been updated", film.getName());
        return filmStorage.updateFilm(film);
    }

    public List<Film> getFilms() {
        log.info("A list of all movies is requested");
        List<Film> films = filmStorage.getFilmsWithoutGenres();
        return populateFilmsWithGenresAndDirectors(films);
    }

    public Film getFilmById(Long id) {
        validateFilm(id);
        log.info("Movie with id: {}, is requested", id);
        return filmStorage.getFilmById(id);
    }

    public void removeFilmById(Long id) {
        validateFilm(id);
        log.info("Movie with id: {}, is removed from collection", id);
        filmStorage.removeFilmById(id);
    }

    public List<Film> getFilmsByDirector(Long directorId, String sortBy) {
        validateDirector(directorId);
        return filmStorage.getFilmsByDirector(directorId, sortBy);
    }

    public List<Film> searchFilmsByTitleOrDirector(String query, String searchBy) {
        log.info("Search query is received: {}. Search parameter: {}", query, searchBy);
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
        log.info("A list of shared movies of users with id: {} and {} is requested", userId, friendId);
        List<Film> films = filmStorage.getCommonFilms(userId, friendId);
        return populateFilmsWithGenresAndDirectors(films);
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
