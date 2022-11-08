package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.Collection;

import static ru.yandex.practicum.filmorate.validator.Validator.validateDirector;
import static ru.yandex.practicum.filmorate.validator.Validator.validateDirectorNotExist;

@Service
@Slf4j
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorStorage directorStorage;

    public Collection<Director> getDirectors() {
        log.info("Запрошен список всех режиссёров");
        return directorStorage.getDirectors();
    }

    public Director getDirectorById(Long id) {
        validateDirector(id);
        log.info("Режиссёр с id: {}, запрошен", id);
        return directorStorage.getDirectorById(id);
    }

    public Director addDirector(Director director) {
        validateDirectorNotExist(director);
        log.info("Режиссёр {} добавлен в хранилище", director.getName());
        return directorStorage.addDirector(director);
    }

    public Director updateDirector(Director director) {
        validateDirector(director.getId());
        log.info("Режиссёр с id: {}, обновлён", director.getId());
        return directorStorage.updateDirector(director);
    }

    public void removeDirectorById(Long id) {
        validateDirector(id);
        log.info("Режиссёр с id: {}, удалён", id);
        directorStorage.removeDirectorById(id);
    }
}
