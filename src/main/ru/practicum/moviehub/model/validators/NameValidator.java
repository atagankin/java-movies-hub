package ru.practicum.moviehub.model.validators;

import java.util.List;

public class NameValidator implements Validator<String> {
    @Override
    public void validate(String value, List<String> errors) {
        if (value.isEmpty()) {
            errors.add("Название фильма не должно быть пустым.");
            //throw new MovieNameException("Название фильма не должно быть пустым.");
        }

        if (value.length() > 100) {
            errors.add("Название фильма не должно превышать 100 символов");
            //throw new MovieNameException("Название фильма не должно превышать 100 символов");
        }
    }
}
