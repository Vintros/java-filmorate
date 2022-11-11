package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.List;

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
        directorStorage.checkDirectorExistsById(id);
        log.info("Director with id: {}, is requested", id);
        return directorStorage.getDirectorById(id);
    }

    public Director addDirector(Director director) {
        if (director.getId() != null) {
            directorStorage.checkDirectorNotExistById(director.getId());
        }
        log.info("Director {} is added to the repository", director.getName());
        return directorStorage.addDirector(director);
    }

    public Director updateDirector(Director director) {
        directorStorage.checkDirectorExistsById(director.getId());
        log.info("Director with id: {}, is updated", director.getId());
        return directorStorage.updateDirector(director);
    }

    public void removeDirectorById(Long id) {
        directorStorage.checkDirectorExistsById(id);
        log.info("Director with id: {}, is deleted", id);
        directorStorage.removeDirectorById(id);
    }
}
