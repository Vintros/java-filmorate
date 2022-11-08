package ru.yandex.practicum.filmorate.storage.feed;

import ru.yandex.practicum.filmorate.model.Event;

import java.util.List;

public interface FeedStorage {

    void saveUserEvent(Event event);

    List<Event> getFeed(Long id);
}
