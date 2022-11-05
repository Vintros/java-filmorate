package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;

import javax.validation.Valid;
import java.util.Collection;

@RestController
@RequestMapping("/directors")
public class DirectorController {
    @GetMapping
    public Collection<Director> getDirectors() {
        return null;
    }

    @GetMapping("/{id}")
    public Director getDirectorByID(@PathVariable Long id) {
        return null;
    }

    @PostMapping
    public Director addDirector(@Valid @RequestBody Director director) {
        return null;
    }

    @PutMapping
    public Director updateDirector(@Valid @RequestBody Director director) {
        return null;
    }

    @DeleteMapping("/id")
    public void removeDirectorById(@PathVariable Long id) {
    }
}
