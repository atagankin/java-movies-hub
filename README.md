# java-movies-hub
Repository for homework project.

## Что не нравится.
* MoviesHandler.postMovie. Для накопления всех ошибок, приходится отдельно запускать каждый валидатор.
```
try {
    yearValidator.validate(year);
} catch (MovieYearException e) {
    errors.add(e.getMessage());
}

try {
    nameValidator.validate(title);
} catch (MovieNameException e) {
    errors.add(e.getMessage());
}
```
* Очень все длинно, размазано - не элегантно...
* MoviesListTypeAdapter - сделал с помощью Deep Seek, но как работает разобрался, похоже на протокол TCP (или WebSocket ?)