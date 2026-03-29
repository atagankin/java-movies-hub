package ru.practicum.moviehub.store;

import ru.practicum.moviehub.model.Movie;

import java.util.*;

public class MoviesStore {
    private final Map<Integer, Movie> store;
    private final Map<Integer, Movie> storeById;

    public MoviesStore() {
        this.store = new HashMap<>();
        this.storeById = new HashMap<>();
    }

    public List<Movie> getMoviesList() {
        return store.values().stream().toList();
    }

    public void flushStore() {
        this.store.clear();
        this.storeById.clear();
    }

    public void addMovie(Movie movie) {
        store.put(movie.hashCode(), movie);
        storeById.put(movie.getId(), movie);
    }

    public Map<Integer, Movie> getStore() {
        return store;
    }

    public Optional<Movie> getMovieById(int id) {
        if (!storeById.containsKey(id)) {
            return Optional.empty();
        }

        return Optional.of(storeById.get(id));
    }

    public Optional<Integer> deleteMovie(int id) {
        if (!storeById.containsKey(id)) {
            return Optional.empty();
        }

        Movie movie = storeById.get(id);
        store.remove(movie.hashCode());
        storeById.remove(id);
        return Optional.of(id);
    }

    public List<Movie> getMoviesByYear(int year) {
        List<Movie> filteredMovies = store.values().stream()
                .filter(m -> m.getYear() == year)
                .toList();

        return filteredMovies;
    }
}