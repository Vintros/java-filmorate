package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;

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
    public List<Film> getMostLikedFilms(@RequestParam(defaultValue = "10") Integer count) {
        return filmService.getMostLikedFilms(count);
    }

    @DeleteMapping("/{filmId}")
    public void removeFilmById(@PathVariable Long filmId) {
        filmService.removeFilmById(filmId);
    }

    @DeleteMapping("{id}/like/{userId}")
    public void removeLikeFilm(@PathVariable Long id, @PathVariable Long userId) {
        filmService.removeLikeFilm(id, userId);
    }
}

