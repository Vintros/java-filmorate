package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.List;

import static ru.yandex.practicum.filmorate.validator.Validator.validateDirector;
import static ru.yandex.practicum.filmorate.validator.Validator.validateDirectorNotExist;

@Service
@Slf4j
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorStorage directorStorage;

    public List<Director> getDirectors() {
        log.info("A list of all directors is requested");
        return directorStorage.getDirectors();
    }

    public Director getDirectorById(Long id) {
        validateDirector(id);
        log.info("Director with id: {}, is requested", id);
        return directorStorage.getDirectorById(id);
    }

    public Director addDirector(Director director) {
        validateDirectorNotExist(director);
        log.info("Director {} is added to the repository", director.getName());
        return directorStorage.addDirector(director);
    }

    public Director updateDirector(Director director) {
        validateDirector(director.getId());
        log.info("Director with id: {}, is updated", director.getId());
        return directorStorage.updateDirector(director);
    }

    public void removeDirectorById(Long id) {
        validateDirector(id);
        log.info("Director with id: {}, is deleted", id);
        directorStorage.removeDirectorById(id);
    }
}
