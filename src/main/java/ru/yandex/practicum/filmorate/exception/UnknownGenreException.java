package ru.yandex.practicum.filmorate.exception;

public class UnknownGenreException extends RuntimeException {

    public UnknownGenreException(String message) {
        super(message);
    }
}
