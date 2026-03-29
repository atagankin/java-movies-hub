package ru.practicum.moviehub.model.validators;

import java.util.List;

public interface Validator<T> {
    void validate(T value, List<String> errors);
}
