package ru.yandex.practicum.filmorate.exception;

public class UnknownMpaException extends UnknownEntityException {

    public UnknownMpaException(String message, Long id) {
        super(message);
    }
}
