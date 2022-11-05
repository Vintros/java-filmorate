package ru.yandex.practicum.filmorate.exception;

public class UnknownReviewException extends RuntimeException{

    public UnknownReviewException(String message) {
        super(message);
    }
}
