package ru.yandex.practicum.filmorate.storage.genre;

import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface GenreStorage {

    List<Genre> getAllGenres();

    Genre getGenreById(Long id);

    Genre mapRowToGenre(ResultSet rs, int rowNum) throws SQLException;

    List<Genre> getGenresByFilmId(Long id);

    Map<Long, List<Genre>> getGenresByFilmsId();

}
