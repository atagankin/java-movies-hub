package ru.practicum.moviehub.model.validators;

import ru.practicum.moviehub.exceptions.MovieValidateException;

public interface Validator<T> {
    void validate(T value) throws MovieValidateException;
}
