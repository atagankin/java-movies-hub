package ru.practicum.moviehub.model.validators;

import ru.practicum.moviehub.exceptions.MovieNameException;

public class NameValidator implements Validator<String> {
    @Override
    public void validate(String value) throws MovieNameException {
        if (value.isEmpty()) {
            throw new MovieNameException("Название фильма не должно быть пустым.");
        }

        if (value.length() > 100) {
            throw new MovieNameException("Название фильма не должно превышать 100 символов");
        }
    }
}
