package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.List;

import static ru.yandex.practicum.filmorate.validator.Validator.validateGenreId;

@Service
@Slf4j
public class GenreService {

    private final GenreStorage genreStorage;

    public GenreService(GenreStorage genreStorage) {
        this.genreStorage = genreStorage;
    }

    public List<Genre> getAllGenres() {
        log.info("Запрошены все жанры");
        return genreStorage.getAllGenres();
    }

    public Genre getGenreById(Long id) {
        validateGenreId(id);
        log.info("Запрошен жанр с id: {}", id);
        return genreStorage.getGenreById(id);
    }
}
