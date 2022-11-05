package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Map;

public interface FilmStorage {

    Film addFilm(Film film);

    Film updateFilm(Film film);

    List<Film> getFilmsWithoutGenres();

    void addLikeFilm(Long id, Long userId);

    void removeLikeFilm(Long id, Long userId);

    Map<Long, List<Long>> getUsersIdLiked();

    Film getFilmById(Long id);

    void removeFilmById(Long id);

    List<Film> getFilmsByDirector(Long directorId, String sortBy);
}
