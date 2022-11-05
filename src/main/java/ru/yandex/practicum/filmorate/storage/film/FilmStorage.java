package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {

    Film addFilm(Film film);

    Film updateFilm(Film film);

    List<Film> getFilmsWithoutGenres();

    void addLikeFilm(Long id, Long userId);

    void removeLikeFilm(Long id, Long userId);

    Film getFilmById(Long id);

    void removeFilmById(Long id);

    List<Film> getListPopularFilm(long count);

    List<Film> getListPopularFilmSortedByYear(int count, int year);

    List<Film> getListPopularFilmSortedByGenre(int count, long genreId);

    List<Film> findPopularFilmSortedByGenreAndYear(int count, long genreId, int year);


}
