package ru.yandex.practicum.filmorate.storage.genre;

import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface GenreStorage {
    /**
     * Метод возвращает список из всех
     * жанров в хранилище.
     *
     * @return Список всех жанров.
     */
    List<Genre> getAllGenres();

    /**
     * Метод возвращает жанр по его
     * идентификатору.
     *
     * @param id идентификатор жанра.
     * @return Жанр, принадлежащий идентификатору.
     */
    Genre getGenreById(Long id);

    /**
     * Метод парсит ответ БД в объект жанра.
     */
    Genre mapRowToGenre(ResultSet rs, int rowNum) throws SQLException;

    /**
     * Метод возвращает список жанров,
     * принадлежащих фильму.
     *
     * @param id идентификатор фильма.
     * @return Список жанров, принадлежащих
     * фильму.
     */
    List<Genre> getGenresByFilmId(Long id);

    /**
     * Метод возвращает Map, где ключ -
     * идентификатор фильма, а значение -
     * список жанров, принадлежащих этому
     * ключу.
     *
     * @return Map идентификаторов фильма и
     * жанров им принадлежащих.
     */
    Map<Long, List<Genre>> getGenresByFilmsId();
}
