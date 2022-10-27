package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

public interface FilmStorage {

    Film addFilm(Film film);

    Film updateFilm(Film film);

    List<Film> getFilms();

    Film getFilmById(Long id);

    void addLikeFilm(Long id, Long userId);

    void removeLikeFilm(Long id, Long userId);

    List<Genre> getAllGenres();

    Genre getGenreById(Long id);

    List<Mpa> getAllMpa();

    Mpa getMpaById(Long id);
}
