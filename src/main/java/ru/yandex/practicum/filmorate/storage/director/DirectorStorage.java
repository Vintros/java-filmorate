package ru.yandex.practicum.filmorate.storage.director;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface DirectorStorage {
    /**
     * Метод возвращает из хранилища всех
     * режиссёров.
     *
     * @return Коллекция из всех режиссёров
     * в хранилище
     */
    Collection<Director> getDirectors();

    /**
     * Метод возвращает режиссёра по его
     * идентификатору.
     *
     * @param id идентификатор режиссёра.
     * @return Режиссёр принадлежащий
     * идентификатору.
     */
    Director getDirectorById(Long id);

    /**
     * Метод добавляет режиссёра в хранилище.
     *
     * @param director добавляемый режиссёр.
     * @return Добавленный режиссёр.
     */
    Director addDirector(Director director);

    /**
     * Метод обновляет режиссёра в хранилище.
     *
     * @param director обновлённый режиссёр.
     * @return Обновлённый режиссёр.
     */
    Director updateDirector(Director director);

    /**
     * Метод удаляет режиссёра из хранилища.
     *
     * @param id идентификатор режиссёра.
     */
    void removeDirectorById(Long id);

    Map<Long, List<Director>> getGenresByFilmsId();

    List<Director> getDirectorsByFilmId(Long id);
}
