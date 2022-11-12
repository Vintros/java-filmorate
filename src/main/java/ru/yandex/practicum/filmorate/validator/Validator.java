package ru.yandex.practicum.filmorate.validator;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.*;

@Service
public class Validator {

    public static void validateGenreId(Long id) {
        if (id < 1 || id > 6) {
            throw new UnknownGenreException("Такой жанр не существует");
        }
    }

    public static void validateMpaId(Long id) {
        if (id < 1 || id > 5) {
            throw new UnknownMpaException("Такая категория не существует");
        }
    }

    public static void validateGenreAndYear(Integer genreId, Integer year) {
        if (year != null && year < 1895 || genreId != null && genreId <= 0) {
            throw new RuntimeException("Неправильный запрос");
        }
    }

    public static void validateSearchParameter(String by) {
        if (!by.equals("title") && !by.equals("director") && !by.equals("title,director") && !by.equals("director,title")) {
            throw new IncorrectSearchParameterException("Введен некорректный параметр поиска. Доступные значения: " +
                    "title; director, title,director; director,title");
        }
    }
}
