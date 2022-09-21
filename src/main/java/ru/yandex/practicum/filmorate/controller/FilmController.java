package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ExistsException;
import ru.yandex.practicum.filmorate.exception.UnnownFilmException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {

    private final Map<Integer, Film> films = new HashMap<>();
    private Integer id = 0;

    @PostMapping
    Film addFilm(@Valid @RequestBody Film film) {
        if (films.containsKey(film.getId())) {
            throw new ExistsException("Такой фильм уже зарегистрирован");
        } else {
            validationFilm(film);
            film.setId(createId());
            films.put(film.getId(), film);
            log.info("Фильм {} добавлен в коллекцию", film.getName());
        }
        return film;
    }

    @PutMapping
    Film updateFilm(@Valid @RequestBody Film film) {
        if (films.containsKey(film.getId())) {
            validationFilm(film);
            films.put(film.getId(), film);
            log.info("Фильм {} обновлен", film.getName());
        } else {
            throw new UnnownFilmException("Неизвестный фильм");
        }
        return film;
    }

    @GetMapping
    List<Film> getFilms() {
        log.info("Запрошен список всех фильмов");
        return new ArrayList<>(films.values());
    }

    private Integer createId() {
        return ++id;
    }

    private void validationFilm(Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Ошибка валидации, дата релиза раньше 28 декабря 1895 года");
        }
    }
}
