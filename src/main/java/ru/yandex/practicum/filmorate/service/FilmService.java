package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.UnknownUserException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.feed.FeedStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

import static ru.yandex.practicum.filmorate.validator.Validator.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final GenreStorage genreStorage;
    private final DirectorStorage directorStorage;
    private final FeedStorage feedStorage;
    private final UserStorage userStorage;

    public void addLikeFilm(Long id, Long userId) {
        userStorage.checkUserExistsById(userId);
        filmStorage.checkFilmExistsById(id);
        filmStorage.checkUserLikeToFilmNotExist(id, userId);

        filmStorage.addLikeFilm(id, userId);
        feedStorage.saveUserEvent(new Event(userId, id, "LIKE", "ADD", new Date()));
        log.debug("User with id: {} has liked the film with id: {}", userId, id);
    }

    public void removeLikeFilm(Long id, Long userId) {
        filmStorage.checkFilmExistsById(id);
        userStorage.checkUserExistsById(userId);

        try {
            filmStorage.removeLikeFilm(id, userId);
        } catch (DataAccessException e) {
            throw new UnknownUserException(String.format("The user with id: %d has not liked the film with id: %d",
                    userId, id));
        }
        feedStorage.saveUserEvent(new Event(userId, id, "LIKE", "REMOVE", new Date()));
        log.debug("A user with id: {} removed a film like with id: {}", userId, id);
    }

    public List<Film> getListPopularFilm(Integer count, Integer genreId, Integer year) {
        validateGenreAndYear(genreId, year);

        List<Film> films;
        if (genreId != null && year != null) {
            log.info("{} popular films by genre #{} and {} year is/are requested", count, genreId, year);
            films = filmStorage.findPopularFilmSortedByGenreAndYear(count, genreId, year);
        } else if (genreId != null) {
            log.info("{} popular films by genre No.{} is/are requested", count, genreId);
            films = filmStorage.getListPopularFilmSortedByGenre(count, genreId);
        } else if (year != null) {
            log.info("{} popular films by {} year is/are requested", count, year);
            films = filmStorage.getListPopularFilmSortedByYear(count, year);
        } else {
            log.info("{} popular films is/are requested", count);
            films = filmStorage.getListPopularFilm(count);
        }
        return populateFilmsWithGenresAndDirectors(films);
    }

    public Film addFilm(Film film) {
        if (film.getId() != null) {
            filmStorage.checkFilmNotExistById(film.getId());
        }

        Film addedFilm = filmStorage.addFilm(film);
        log.info("Film - {} is added to collection", film.getName());
        return populateFilmWithGenresAndDirectors(addedFilm);
    }

    public Film updateFilm(Film film) {
        filmStorage.checkFilmExistsById(film.getId());

        Film updatedFilm = filmStorage.updateFilm(film);
        log.info("The film - {} has been updated", updatedFilm.getName());
        return populateFilmWithGenresAndDirectors(updatedFilm);
    }

    public List<Film> getFilms() {
        List<Film> films = filmStorage.getFilmsWithoutGenresAndDirectors();
        log.info("A list of all films is requested");
        return populateFilmsWithGenresAndDirectors(films);
    }

    public Film getFilmById(Long id) {
        filmStorage.checkFilmExistsById(id);

        Film film = filmStorage.getFilmByIdWithoutGenresAndDirectors(id);
        log.info("Film with id: {} is requested", id);
        return populateFilmWithGenresAndDirectors(film);
    }

    public void removeFilmById(Long id) {
        filmStorage.checkFilmExistsById(id);

        filmStorage.removeFilmById(id);
        log.info("Film with id: {} is removed from collection", id);
    }

    public List<Film> getFilmsByDirector(Long directorId, String sortBy) {
        directorStorage.checkDirectorExistsById(directorId);
        log.info("A list of films by director with id: {}, sorted by: {}", directorId, sortBy);

        List<Film> films = filmStorage.getFilmsByDirectorWithoutGenresAndDirectors(directorId, sortBy);
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

    public List<Film> searchFilmsByTitleOrDirector(String query, String searchBy) {
        log.info("Search query is received: {}. Search parameter: {}", query, searchBy);
        validateSearchParameter(searchBy);

        List<Map.Entry<Long, String>> dataList = new ArrayList<>();
        switch (searchBy) {
            case "title":
                dataList.addAll(filmStorage.searchFilmsWithoutGenresAndDirectorsByTitle(query));
                break;
            case "director":
                dataList.addAll(filmStorage.searchFilmsWithoutGenresAndDirectorsByDirector(query));
                break;
            case "title,director":
            case "director,title":
                dataList.addAll(filmStorage.searchFilmsWithoutGenresAndDirectorsByTitle(query));
                dataList.addAll(filmStorage.searchFilmsWithoutGenresAndDirectorsByDirector(query));
                break;
        }
        List<Long> matchingIds = getMatchingIds(query, dataList);
        List<Film> films = filmStorage.getFilmsSortedByPopularity(matchingIds);

        return populateFilmsWithGenresAndDirectors(films);
    }

    public List<Film> getCommonFilms(Long userId, Long friendId) {
        log.info("A list of shared films of users with id: {} and {} is requested", userId, friendId);
        userStorage.checkUserExistsById(userId);
        userStorage.checkUserExistsById(friendId);


        List<Film> films = filmStorage.getCommonFilms(userId, friendId);
        return populateFilmsWithGenresAndDirectors(films);
    }

    public List<Film> getRecommendedFilms(Long id) {
        userStorage.checkUserExistsById(id);

        List<Film> films = new ArrayList<>();

        List<Map.Entry<Long, Long>> entriesUserIdLikedFilmId = filmStorage.getEntriesUserIdLikedFilmId();
        if (entriesUserIdLikedFilmId.isEmpty()) {
            return films;
        }
        Map<Long, ArrayList<Long>> usersIdWithLikedFilmsId = getUsersIdWithLikedFilmsId(entriesUserIdLikedFilmId);
        Long mostIntersectionsUserId = getMostIntersectionsUserId(id, usersIdWithLikedFilmsId);
        if (mostIntersectionsUserId == null) {
            return films;
        }
        List<Long> filmsId = new ArrayList<>();
        for (Long filmId : usersIdWithLikedFilmsId.get(mostIntersectionsUserId)) {
            if (!usersIdWithLikedFilmsId.get(id).contains(filmId)) {
                filmsId.add(filmId);
            }
        }
        films.addAll(filmStorage.getFilmsSortedByPopularity(filmsId));
        log.info("A user with id: {} requested a list of recommended films", id);
        return populateFilmsWithGenresAndDirectors(films);
    }

    private Film populateFilmWithGenresAndDirectors(Film film) {
        List<Genre> genres = genreStorage.getGenresByFilmId(film.getId());
        film.getGenres().addAll(genres);
        List<Director> directors = directorStorage.getDirectorsByFilmId(film.getId());
        film.getDirectors().addAll(directors);
        return film;
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

    private List<Long> getMatchingIds(String query, List<Map.Entry<Long, String>> dataList) {
        List<Long> matchingIds = new ArrayList<>();
        for (Map.Entry<Long, String> entry : dataList) {
            if (entry.getValue().toLowerCase().contains(query.toLowerCase())) {
                matchingIds.add(entry.getKey());
            }
        }
        return matchingIds.stream()
                .distinct()
                .collect(Collectors.toList());
    }

    private Long getMostIntersectionsUserId(Long requestedUserId, Map<Long, ArrayList<Long>> usersIdWithLikedFilmsId) {
        Map<Long, Long> frequency = new HashMap<>();

        for (Map.Entry<Long, ArrayList<Long>> userIdWithLikedFilmsId : usersIdWithLikedFilmsId.entrySet()) {
            if (!userIdWithLikedFilmsId.getKey().equals(requestedUserId)) {
                Long intersectionsCount = usersIdWithLikedFilmsId.get(requestedUserId)
                        .stream()
                        .filter((userIdWithLikedFilmsId.getValue()::contains))
                        .count();
                frequency.put(userIdWithLikedFilmsId.getKey(), intersectionsCount);
            }
        }
        Optional<Map.Entry<Long, Long>> mostIntersectionsUser = frequency.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue());

        return mostIntersectionsUser.map(Map.Entry::getKey).orElse(null);
    }

    private Map<Long, ArrayList<Long>> getUsersIdWithLikedFilmsId(List<Map.Entry<Long, Long>> entriesUserIdLikedFilmId) {
        Map<Long, ArrayList<Long>> usersIdWithLikedFilmsId = new HashMap<>();
        for (Map.Entry<Long, Long> entryUserIdLikedFilmsId : entriesUserIdLikedFilmId) {
            Long userId = entryUserIdLikedFilmsId.getKey();
            if (usersIdWithLikedFilmsId.containsKey(userId)) {
                Long filmId = entryUserIdLikedFilmsId.getValue();
                usersIdWithLikedFilmsId.get(userId).add(filmId);
            } else {
                usersIdWithLikedFilmsId.put(userId,
                        new ArrayList<>(Collections.singletonList(entryUserIdLikedFilmsId.getValue())));
            }
        }
        return usersIdWithLikedFilmsId;
    }
}
