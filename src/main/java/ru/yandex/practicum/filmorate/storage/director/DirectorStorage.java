package ru.yandex.practicum.filmorate.storage.director;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface DirectorStorage {
    /**
     * Метод возвращает список всех
     * режиссёров.
     *
     * @return Список всех режиссёров
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

    /**
     * Метод возвращает Map, где ключ -
     * идентификатор фильма, а значение -
     * список режиссёров, принадлежащих этому
     * ключу.
     *
     * @return Map идентификаторов фильма и
     * режиссёров им принадлежащих.
     */
    Map<Long, List<Director>> getDirectorsByFilmsId();

    /**
     * Метод возвращает список режиссёров,
     * принадлежащих фильму.
     *
     * @param id идентификатор фильма.
     * @return Список режиссёров, принадлежащих
     * идентификатору фильма.
     */
    List<Director> getDirectorsByFilmId(Long id);
}
