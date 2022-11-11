package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.List;

import static ru.yandex.practicum.filmorate.validator.Validator.validateGenreId;

@Service
@Slf4j
@RequiredArgsConstructor
public class GenreService {

    private final GenreStorage genreStorage;

    public List<Genre> getAllGenres() {
        log.info("All genres are requested");
        return genreStorage.getAllGenres();
    }

    public Genre getGenreById(Long id) {
        validateGenreId(id);
        log.info("The genre with id: {} is requested", id);
        return genreStorage.getGenreById(id);
    }
}
