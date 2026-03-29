package ru.practicum.moviehub.model.validators;

import java.time.LocalDate;
import java.util.List;

public class YearValidator implements Validator<String> {
    @Override
    public void validate(String value, List<String> errors) {
        int currYear = LocalDate.now().getYear();

        try {
            int requestValue = Integer.parseInt(value);
            if (requestValue < 1888 || requestValue > currYear + 1) {
                errors.add("Год фильма должен быть в диапазоне между 1888 и " + (currYear + 1));
            }
        } catch (NumberFormatException e) {
            errors.add("Год должен быть числом.");
        }
    }
}
