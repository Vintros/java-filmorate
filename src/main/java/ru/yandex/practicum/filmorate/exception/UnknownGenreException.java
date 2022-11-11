package ru.yandex.practicum.filmorate.exception;

public class UnknownGenreException extends UnknownEntityException {

    public UnknownGenreException(String message, Long id) {
        super(message);
    }
}
