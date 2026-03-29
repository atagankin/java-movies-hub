package ru.practicum.moviehub.http;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.exceptions.MovieNotFound;
import ru.practicum.moviehub.exceptions.MovieValidateException;
import ru.practicum.moviehub.model.validators.NameValidator;
import ru.practicum.moviehub.model.validators.YearValidator;
import ru.practicum.moviehub.store.MoviesStore;


public class MoviesHandler extends BaseHttpHandler {
    private final MoviesStore movieStore;

    public MoviesHandler(MoviesStore movieStore) {
        this.movieStore = movieStore;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        Endpoint endpoint = getEndpoint(ex.getRequestURI(), ex.getRequestMethod());
        switch (endpoint) {
            case GET_MOVIES -> this.sendJson(ex, 200, this.getMovies());
            case POST_MOVIE -> this.postMovie(ex);
            case GET_MOVIE_BY_ID -> this.getMovieByID(ex);
            case DELETE_MOVIE -> this.deleteById(ex);
            case GET_MOVIES_BY_YEAR -> this.getMoviesByYear(ex);
            case UNKNOWN -> sendJson(ex, 405, gson.toJson(new ErrorResponse("METHOD_NOT_ALLOWED")));
        }
    }

    private Endpoint getEndpoint(URI requestURL, String requestMethod) {
        String[] paths = requestURL.getPath().split("/");

        switch (requestMethod) {
            case "GET":
                if (paths.length == 2) {
                    if (requestURL.getQuery() != null) {
                        return Endpoint.GET_MOVIES_BY_YEAR;
                    } else {
                        return Endpoint.GET_MOVIES;
                    }
                } else if (paths.length == 3) {
                    return Endpoint.GET_MOVIE_BY_ID;
                } else {
                    return Endpoint.UNKNOWN;
                }
            case "POST":
                if (paths.length == 2) {
                    return Endpoint.POST_MOVIE;
                } else {
                    return Endpoint.UNKNOWN;
                }
            case "DELETE":
                if (paths.length == 3) {
                    return Endpoint.DELETE_MOVIE;
                } else {
                    return Endpoint.UNKNOWN;
                }
            default:
                return Endpoint.UNKNOWN;
        }
    }

    private String getMovies() {
        return gson.toJson(movieStore.getMoviesList());
    }

    private void getMoviesByYear(HttpExchange exchange) throws IOException {
        Optional<String> yearParameter = getQueryParam(exchange.getRequestURI(), "year");

        try {
            if (yearParameter.isEmpty() || yearParameter.get().isEmpty()) {
                throw new InvalidPropertiesFormatException("Некорректный параметр запроса — 'year'");
            }

            int year = Integer.parseInt(yearParameter.get());
            this.sendJson(exchange, 200, gson.toJson(movieStore.getMoviesByYear(year)));

        } catch (InvalidPropertiesFormatException e) {
            sendJson(exchange, 400, gson.toJson(new ErrorResponse(e.getMessage())));
        } catch (NumberFormatException e) {
            sendJson(exchange, 400, gson.toJson(new ErrorResponse("Некорректный параметр запроса — 'year'")));
        }
    }

    private Optional<String> getQueryParam(URI url, String parameter) {
        String filter = url.getQuery();
        if (filter == null) {
            return Optional.empty();
        }

        return Stream.of(filter.split("&"))
                .filter(s -> s.contains(parameter + "="))
                .map(s -> s.split("="))
                .filter(strings -> strings.length == 2)
                .map(strings -> strings[1])
                .findFirst();
    }

    private void getMovieByID(HttpExchange exchange) throws IOException {
        String[] paths = exchange.getRequestURI().getPath().split("/");
        int searchedId;

        try {
            searchedId = Integer.parseInt(paths[paths.length - 1]);
            Optional<Movie> movie = movieStore.getMovieById(searchedId);
            if (movie.isEmpty()) {
                throw new MovieNotFound("Фильм с id = " + searchedId + " не найден.");
            }
            this.sendJson(exchange, 200, gson.toJson(movie.get()));
        } catch (NumberFormatException e) {
            sendJson(exchange, 400, gson.toJson(new ErrorResponse("Параметр id должен быть целочисленным.")));
        } catch (MovieNotFound e) {
            sendJson(exchange, 404, gson.toJson(new ErrorResponse(e.getMessage())));
        }
    }

    public void deleteById(HttpExchange exchange) throws IOException {
        String[] paths = exchange.getRequestURI().getPath().split("/");
        int searchedId;

        try {
            searchedId = Integer.parseInt(paths[paths.length - 1]);
            Optional<Integer> deleted = movieStore.deleteMovie(searchedId);
            if (deleted.isEmpty()) {
                throw new MovieNotFound("Фильм с id = " + searchedId + " не найден.");
            }
            this.sendNoContent(exchange);
        } catch (NumberFormatException e) {
            sendJson(exchange, 400, gson.toJson(new ErrorResponse("Параметр id должен быть целочисленным.")));
        } catch (MovieNotFound e) {
            sendJson(exchange, 404, gson.toJson(new ErrorResponse(e.getMessage())));
        }
    }

    private void postMovie(HttpExchange exchange) throws IOException {

        List<String> errors = new ArrayList<>();
        YearValidator yearValidator = new YearValidator();
        NameValidator nameValidator = new NameValidator();

        // Проверка по Content-Type
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        if (contentType == null || !contentType.contains("application/json")) {
            sendJson(exchange, 415, gson.toJson(new ErrorResponse("Content-Type должен быть application/json")));
            return;
        }

        try (InputStream in = exchange.getRequestBody()) {
            String body = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            JsonElement jsonElement = JsonParser.parseString(body);

            if (!jsonElement.isJsonObject()) {
                throw new JsonSyntaxException("Не является JSON объектом.");
            }

            JsonObject json = jsonElement.getAsJsonObject();
            if (!json.has("title") || !json.has("year")) {
                throw new MovieValidateException("Должны быть заданы title и year");
            }

            nameValidator.validate(json.get("title").getAsString(), errors);
            yearValidator.validate(json.get("year").getAsString(), errors);

            if (!errors.isEmpty()) {
                sendJson(exchange, 422,
                        gson.toJson(new ErrorResponse("Ошибка валидации фильма", errors.toArray(new String[0]))));
            } else {
                String title = json.get("title").getAsString();
                int year = json.get("year").getAsInt();

                Movie movie;
                movie = new Movie(title, year);
                if (movieStore.getStore().containsKey(movie.hashCode())) {
                    sendJson(exchange, 201, gson.toJson(movieStore.getStore().get(movie.hashCode())));
                } else {
                    movieStore.addMovie(movie);
                    sendJson(exchange, 201, gson.toJson(movie));
                }
            }
        } catch (JsonSyntaxException e) {
            sendJson(exchange, 400, gson.toJson(new ErrorResponse(e.getMessage())));
        } catch (MovieValidateException e) {
            sendJson(exchange, 422, gson.toJson(new ErrorResponse(e.getMessage())));
        }
    }
}
