package ru.practicum.moviehub.http;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import ru.practicum.moviehub.model.Movie;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MoviesListTypeAdapter extends TypeAdapter<List<Movie>> {
    @Override
    public void write(JsonWriter out, List<Movie> movies) throws IOException {
        out.beginArray();
        for (Movie movie : movies) {
            out.beginObject();
            out.name("id").value(movie.getId());
            out.name("title").value(movie.getTitle()); // нужен геттер
            out.name("year").value(movie.getYear());
            out.endObject();
        }
        out.endArray();
    }

    @Override
    public List<Movie> read(JsonReader in) throws IOException {
        List<Movie> movies = new ArrayList<>();

        in.beginArray();
        while (in.hasNext()) {
            int id = 0;
            String title = null;
            int year = 0;

            in.beginObject();
            while (in.hasNext()) {
                switch (in.nextName()) {
                    case "id":
                        id = in.nextInt();
                        break;
                    case "title":
                        title = in.nextString();
                        break;
                    case "year":
                        year = in.nextInt();
                        break;
                }
            }
            in.endObject();

            movies.add(new Movie(id, title, year));
        }
        in.endArray();

        return movies;
    }
}
