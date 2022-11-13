package ru.yandex.practicum.filmorate.exception;

public class UnknownEntityException extends RuntimeException {

    public UnknownEntityException(String message) {
        super(message);
    }
}
