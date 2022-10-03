package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ExistsException;
import ru.yandex.practicum.filmorate.exception.UnknownFilmException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();
    private long id = 0;
    private final LocalDate MOVIE_BIRTHDAY = LocalDate.of(1895, 12, 28);

    @Override
    public Film addFilm(Film film) {
        if (films.containsKey(film.getId())) {
            throw new ExistsException("Такой фильм уже зарегистрирован");
        }
        validationFilm(film);
        film.setId(createId());
        films.put(film.getId(), film);
        log.info("Фильм {} добавлен в коллекцию", film.getName());
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        if (!films.containsKey(film.getId())) {
            throw new UnknownFilmException("Неизвестный фильм");
        }
        validationFilm(film);
        films.put(film.getId(), film);
        log.info("Фильм {} обновлен", film.getName());
        return film;
    }

    @Override
    public List<Film> getFilms() {
        log.info("Запрошен список всех фильмов");
        return new ArrayList<>(films.values());
    }

    @Override
    public Film getFilmById(Long id) {
        if (!films.containsKey(id)) {
            throw new UnknownFilmException("Неизвестный фильм");
        }
        log.info("Фильм с id: {}, запрошен", id);
        return films.get(id);
    }

    private long createId() {
        return ++id;
    }

    private void validationFilm(Film film) {
        if (film.getReleaseDate().isBefore(MOVIE_BIRTHDAY)) {
            throw new ValidationException("Ошибка валидации, дата релиза раньше 28 декабря 1895 года");
        }
    }
}

