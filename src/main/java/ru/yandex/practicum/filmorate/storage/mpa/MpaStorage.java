package ru.yandex.practicum.filmorate.storage.mpa;

import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

public interface MpaStorage {
    /**
     * Метод возвращает из хранилища список
     * всех рейтингов MPA.
     *
     * @return Список всех рейтингов MPA.
     */
    List<Mpa> getAllMpa();

    /**
     * Метод возвращает рейтинг MPA по его
     * идентификатору.
     *
     * @param id идентификатор рейтинга.
     * @return Рейтинг MPA, принадлежащий
     * идентификатору.
     */
    Mpa getMpaById(Long id);
}
