package ru.yandex.practicum.filmorate.exception;

public class UnknownDirectorException extends RuntimeException {
    public UnknownDirectorException(String message) {
        super(message);
    }
}
