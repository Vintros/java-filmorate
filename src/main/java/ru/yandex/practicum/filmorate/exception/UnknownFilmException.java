package ru.yandex.practicum.filmorate.exception;

public class UnknownFilmException extends RuntimeException {

    public UnknownFilmException(String message) {
        super(message);
    }
}
