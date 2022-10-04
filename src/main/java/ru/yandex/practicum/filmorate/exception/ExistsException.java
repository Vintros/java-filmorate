package ru.yandex.practicum.filmorate.exception;

public class ExistsException extends RuntimeException {

    public ExistsException(String message) {
        super(message);
    }
}
