package ru.practicum.moviehub.model.validators;

import ru.practicum.moviehub.exceptions.MovieYearException;

import java.time.LocalDate;

public class YearValidator implements Validator<Integer> {
    @Override
    public void validate(Integer value) throws MovieYearException {
        int currYear = LocalDate.now().getYear();

        if (value < 1888 || value > currYear + 1) {
            throw new MovieYearException("Год фильма должен быть в диапазоне между 1888 и " + (currYear + 1));
        }
    }
}
