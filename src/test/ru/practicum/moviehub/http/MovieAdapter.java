package ru.practicum.moviehub.http;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import ru.practicum.moviehub.model.Movie;

import java.io.IOException;

public class MovieAdapter extends TypeAdapter<Movie> {
    @Override
    public Movie read(final JsonReader in) throws IOException {
        int id = 0;
        String title = "";
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

        return new Movie(id, title, year);
    }

    @Override
    public void write(final JsonWriter out, Movie movie) throws IOException {
        out.beginObject();
        out.name("id").value(movie.getId());
        out.name("title").value(movie.getTitle());
        out.name("year").value(movie.getYear());
        out.endObject();
    }
}
