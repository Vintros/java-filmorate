package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.List;

import static ru.yandex.practicum.filmorate.validator.Validator.validateMpaId;

@Service
@Slf4j
public class MpaService {

    private final MpaStorage mpaStorage;

    public MpaService(MpaStorage mpaStorage) {
        this.mpaStorage = mpaStorage;
    }

    public List<Mpa> getAllMpa() {
        log.info("Запрошены все рейтинги");
        return mpaStorage.getAllMpa();
    }

    public Mpa getMpaById(Long id) {
        validateMpaId(id);
        log.info("Запрошен рейтинг с id: {}", id);
        return mpaStorage.getMpaById(id);
    }
}
