package ru.yandex.practicum.filmorate.exception;

public class UnknownUserException extends UnknownEntityException {

    public UnknownUserException(String message) {
        super(message);
    }
}
