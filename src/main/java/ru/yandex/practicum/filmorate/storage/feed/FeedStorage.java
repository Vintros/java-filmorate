package ru.yandex.practicum.filmorate.storage.feed;

import ru.yandex.practicum.filmorate.model.Event;

import java.util.List;

public interface FeedStorage {
    /**
     * Метод записывает событие в хранилище.
     *
     * @param event объект события.
     */
    void saveUserEvent(Event event);

    /**
     * Метод возвращает список событий
     * связанных с пользователем.
     *
     * @param id идентификатор пользователя.
     * @return Список всех событий связанных
     * с пользователем.
     */
    List<Event> getFeed(Long id);
}
