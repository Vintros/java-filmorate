package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;

import ru.yandex.practicum.filmorate.exception.IncorrectSearchParameterException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {
        return filmService.addFilm(film);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLikeFilm(@PathVariable Long id, @PathVariable Long userId) {
        filmService.addLikeFilm(id, userId);
    }

    @PutMapping()
    public Film updateFilm(@Valid @RequestBody Film film) {
        return filmService.updateFilm(film);
    }

    @GetMapping
    public List<Film> getFilms() {
        return filmService.getFilms();
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable Long id) {
        return filmService.getFilmById(id);
    }

    @GetMapping("/popular")
    public List<Film> getListPopularFilm(@RequestParam(defaultValue = "10") Integer count,
                                         @RequestParam(required = false) Integer genreId,
                                         @RequestParam(required = false) Integer year) {
        return filmService.getListPopularFilm(count, genreId, year);
    }

    @GetMapping("/director/{directorId}")
    public List<Film> getFilmsByDirector(@PathVariable Long directorId, @RequestParam String sortBy) {
        return filmService.getFilmsByDirector(directorId, sortBy);
    }

    @DeleteMapping("/{filmId}")
    public void removeFilmById(@PathVariable Long filmId) {
        filmService.removeFilmById(filmId);
    }

    @DeleteMapping("{id}/like/{userId}")
    public void removeLikeFilm(@PathVariable Long id, @PathVariable Long userId) {
        filmService.removeLikeFilm(id, userId);
    }

    @GetMapping("/search")
    public List<Film> searchFilmsByTitleOrDirector(@RequestParam String query,
                                                @RequestParam(defaultValue = "title") String by) {
        validateSearchParameter(by);
        return filmService.searchFilmsByTitleOrDirector(query, by);
    }

    private static void validateSearchParameter(String by) {
        if (!by.equals("title") && !by.equals("director") && !by.equals("title,director") && !by.equals("director,title")) {
            throw new IncorrectSearchParameterException("Введен некорректный параметр поиска. Доступные значения: title; director, title,director; director,title");
        }
    }
}

