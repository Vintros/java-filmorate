package ru.yandex.practicum.filmorate.exception;

public class UnknownUserException extends RuntimeException {

    public UnknownUserException(String message) {
        super(message);
    }
}
