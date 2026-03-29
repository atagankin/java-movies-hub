package ru.practicum.moviehub.exceptions;

public class MovieNotFound extends RuntimeException {
    public MovieNotFound(String message) {
        super(message);
    }
}
