package ru.practicum.moviehub.exceptions;

public class MovieValidateException extends RuntimeException {
    public MovieValidateException(String message) {
        super(message);
    }
}
