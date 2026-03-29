package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.*;
import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MoviesApiTest {
    private static final String BASE = "http://localhost:8080"; // !!! добавьте базовую часть URL
    private static MoviesServer server;
    private static HttpClient client;

    @BeforeAll
    static void beforeAll() {
        server = new MoviesServer(new MoviesStore(), 8080);
        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
        server.start();
    }

    @BeforeEach
    void beforeEach() {
        server.getMovieStore().flushStore();
    }

    @AfterAll
    static void afterAll() {
        server.stop();
    }

    @DisplayName("Пустой список")
    @Test
    void getMovies_whenEmpty_returnsEmptyArray() throws Exception {
        ResponseRecord rec = RequestRunner.runGetRequest(URI.create(BASE + "/movies"), client);

        assertEquals(200, rec.statusCode(), "GET /movies должен вернуть 200");
        assertEquals("application/json; charset=UTF-8", rec.paramContentType(),
                "Content-Type должен содержать формат данных и кодировку");

        String body = rec.responseBody().trim();
        assertTrue(body.startsWith("[") && body.endsWith("]"), "Ожидается JSON-массив");
    }

    @DisplayName("Список с 1 элементом")
    @Test
    void getMovies_returnsArrayOf1Singlelement() throws Exception {

        List<Movie> sampleList = new ArrayList<>();
        sampleList.add(new Movie("Sample 1", 2000));
        sampleList.forEach(m -> server.getMovieStore().addMovie(m));

        ResponseRecord rec = RequestRunner.runGetRequest(URI.create(BASE + "/movies"), client);

        assertEquals(200, rec.statusCode(), "GET /movies должен вернуть 200");
        assertEquals("application/json; charset=UTF-8", rec.paramContentType(),
                "Content-Type должен содержать формат данных и кодировку");

        // Нужно получить ответ, преобразовать в объект и сравнить объекты
        JsonElement jsonElement = JsonParser.parseString(rec.responseBody());
        if (!jsonElement.isJsonArray()) {
            fail();
        }

        List<Movie> resultList = new GsonBuilder()
                .registerTypeAdapter(new TypeToken<List<Movie>>(){}.getType(), new MoviesListTypeAdapter())
                .create()
                .fromJson(rec.responseBody(), new TypeToken<List<Movie>>(){}.getType());

        assertArrayEquals(sampleList.toArray(), resultList.toArray());
    }

    @DisplayName("Список с 3мя элементами")
    @Test
    void getMovies_returnsArrayOf3lements() throws Exception {
        List<Movie> sampleList = new ArrayList<>();
        sampleList.add(new Movie("Sample 1", 2000));
        sampleList.add(new Movie("Sample 2", 2001));
        sampleList.add(new Movie("Sample 3", 2002));
        sampleList.forEach(m -> server.getMovieStore().addMovie(m));

        ResponseRecord rec = RequestRunner.runGetRequest(URI.create(BASE + "/movies"), client);
        assertEquals(200, rec.statusCode(), "GET /movies должен вернуть 200");
        assertEquals("application/json; charset=UTF-8", rec.paramContentType(),
                "Content-Type должен содержать формат данных и кодировку");

        List<Movie> resultList = new GsonBuilder()
                .registerTypeAdapter(new TypeToken<List<Movie>>(){}.getType(), new MoviesListTypeAdapter())
                .create()
                .fromJson(rec.responseBody(), new TypeToken<List<Movie>>(){}.getType());

        assertArrayEquals(sampleList.toArray(), resultList.toArray());
    }

    @DisplayName("Добавить фильм")
    @Test
    void postMovie_returnSuccess() throws Exception {

        Movie movie = new Movie(1,"Sample 1", 2026);
        String requestBoby = "{\"title\":\"" + movie.getTitle() + "\", \"year\":" + movie.getYear() + "}";

        ResponseRecord rec = RequestRunner.runPostRequest(
                URI.create(BASE + "/movies"),
                client,
                requestBoby,
                true);

        assertEquals(201, rec.statusCode(), "POST /movies должен вернуть 201");
        assertEquals("application/json; charset=UTF-8", rec.paramContentType(),
                "Content-Type должен содержать формат данных и кодировку");

        try {
            Movie target = new GsonBuilder()
                    .registerTypeAdapter(Movie.class, new MovieAdapter())
                    .create()
                    .fromJson(rec.responseBody(), Movie.class);

            assertEquals(movie, target);
        } catch (Exception e) {
            fail();
        }
    }

    @DisplayName("POST без заголовка Content-Type")
    @Test
    void postMovie_shouldReturn415() throws Exception {
        Movie movie = new Movie(1,"Sample 1", 2026);
        String requestBoby = "{\"title\":\"" + movie.getTitle() + "\", \"year\":" + movie.getYear() + "}";

        ResponseRecord rec = RequestRunner.runPostRequest(
                URI.create(BASE + "/movies"),
                client,
                requestBoby,
                false);

        assertEquals(415, rec.statusCode());
    }

    @DisplayName("POST с ошибкой валидации")
    @Test
    void postMovie_shouldReturn422() throws Exception {
        Movie movie = new Movie(1,"Sample 1", 20260);
        String requestBoby = "{\"title\":\"" + movie.getTitle() + "\", \"year\":" + movie.getYear() + "}";

        ResponseRecord rec = RequestRunner.runPostRequest(
                URI.create(BASE + "/movies"),
                client,
                requestBoby,
                true);

        assertEquals(422, rec.statusCode());
    }

    @DisplayName("POST с 2мя ошибками валидации")
    @Test
    void postMovie_shouldReturn422And2Error() throws Exception {
        String requestBoby = "{\"title\":\"" + "" + "\", \"year\":" + 20260 + "}";

        ResponseRecord rec = RequestRunner.runPostRequest(
                URI.create(BASE + "/movies"),
                client,
                requestBoby,
                true);

        assertEquals(422, rec.statusCode());
        ErrorResponse error = new GsonBuilder()
                .create()
                .fromJson(rec.responseBody(), ErrorResponse.class);

        assertEquals(2, error.getDetails().length);
    }

    @DisplayName("POST: год - строка")
    @Test
    void postMovie_shouldReturn415YearException() throws Exception {
        String requestBoby = "{\"title\":\"Sample 1\", \"year\":y2026}";

        ResponseRecord rec = RequestRunner.runPostRequest(
                URI.create(BASE + "/movies"),
                client,
                requestBoby,
                true);

        assertEquals(415, rec.statusCode());
    }

    @DisplayName("GET. Поиск заказ по ID")
    @Test
    void getMovie_shouldReturnMovie() throws Exception {
        List<Movie> sampleList = new ArrayList<>();
        sampleList.add(new Movie(1, "Sample 1", 2000));
        sampleList.add(new Movie(2, "Sample 2", 2001));
        sampleList.add(new Movie(3, "Sample 3", 2002));
        sampleList.forEach(m -> server.getMovieStore().addMovie(m));

        ResponseRecord rec = RequestRunner.runGetRequest(URI.create(BASE + "/movies/1"), client);

        assertEquals(200, rec.statusCode(), "GET /movies/{id} должен вернуть 200");
        assertEquals("application/json; charset=UTF-8", rec.paramContentType(),
                "Content-Type должен содержать формат данных и кодировку");

        Movie target = new GsonBuilder()
                .registerTypeAdapter(Movie.class, new MovieAdapter())
                .create()
                .fromJson(rec.responseBody(), Movie.class);

        System.out.println("target = " + target);
        assertEquals(sampleList.get(0), target);
    }

    @DisplayName("GET. Поиск по ID, заказ не найден")
    @Test
    void getMovie_shouldReturn404() throws Exception {
        List<Movie> sampleList = new ArrayList<>();
        sampleList.add(new Movie(1, "Sample 1", 2000));
        sampleList.add(new Movie(2, "Sample 2", 2001));
        sampleList.add(new Movie(3, "Sample 3", 2002));
        sampleList.forEach(m -> server.getMovieStore().addMovie(m));

        ResponseRecord rec = RequestRunner.runGetRequest(URI.create(BASE + "/movies/5"), client);

        assertEquals(404, rec.statusCode(), "GET /movies/{id} должен вернуть 404");
        assertEquals("application/json; charset=UTF-8", rec.paramContentType(),
                "Content-Type должен содержать формат данных и кодировку");
    }

    @DisplayName("GET. Поиск по ID, ошибка формата ID")
    @Test
    void getMovie_shouldReturn400() throws Exception {
        ResponseRecord rec = RequestRunner.runGetRequest(URI.create(BASE + "/movies/A1"), client);

        assertEquals(400, rec.statusCode());
        assertEquals("application/json; charset=UTF-8", rec.paramContentType(),
                "Content-Type должен содержать формат данных и кодировку");
    }

    @DisplayName("Успешное удаление")
    @Test
    void deleteMovie_shouldDeleteSuccessfully() throws Exception {
        List<Movie> sampleList = new ArrayList<>();
        sampleList.add(new Movie(1, "Sample 1", 2000));
        sampleList.add(new Movie(2, "Sample 2", 2001));
        sampleList.add(new Movie(3, "Sample 3", 2002));
        sampleList.forEach(m -> server.getMovieStore().addMovie(m));

        ResponseRecord rec = RequestRunner.runDeleteRequest(
                URI.create(BASE + "/movies/1"),
                client);

        assertEquals(204, rec.statusCode());
        assertEquals(2, server.getMovieStore().getStore().size());
    }

    @DisplayName("Удаление - фильм не найден")
    @Test
    void deleteMovie_shouldReturn404NotFound() throws Exception {
        List<Movie> sampleList = new ArrayList<>();
        sampleList.add(new Movie(1, "Sample 1", 2000));
        sampleList.add(new Movie(2, "Sample 2", 2001));
        sampleList.add(new Movie(3, "Sample 3", 2002));
        sampleList.forEach(m -> server.getMovieStore().addMovie(m));

        ResponseRecord rec = RequestRunner.runDeleteRequest(
                URI.create(BASE + "/movies/10"),
                client);

        assertEquals(404, rec.statusCode());
        assertTrue(server.getMovieStore().getMovieById(10).isEmpty());
    }

    @DisplayName("GET по году. Ошибка формата")
    @Test
    void getMoviesByYear_shouldReturn400FormatError() throws Exception {
        List<Movie> sampleList = new ArrayList<>();
        sampleList.add(new Movie(1, "Sample 1", 2026));
        sampleList.add(new Movie(2, "Sample 2", 2001));
        sampleList.add(new Movie(3, "Sample 3", 2002));
        sampleList.forEach(m -> server.getMovieStore().addMovie(m));

        ResponseRecord rec = RequestRunner.runGetRequest(
                URI.create(BASE + "/movies?year=Y2026"),
                client);

        assertEquals(400, rec.statusCode());
    }

    @DisplayName("GET по году. Пустой список")
    @Test
    void getMoviesByYear_shouldReturnEmptyList() throws Exception {
        List<Movie> sampleList = new ArrayList<>();
        sampleList.add(new Movie(1, "Sample 1", 2000));
        sampleList.add(new Movie(2, "Sample 2", 2001));
        sampleList.add(new Movie(3, "Sample 3", 2002));
        sampleList.forEach(m -> server.getMovieStore().addMovie(m));

        ResponseRecord rec = RequestRunner.runGetRequest(
                URI.create(BASE + "/movies?year=2026"),
                client);

        assertEquals(200, rec.statusCode());
        assertEquals("[]", rec.responseBody());
    }

    @DisplayName("GET по году. Непустой список")
    @Test
    void getMoviesByYear_shouldReturnList() throws Exception {
        List<Movie> sampleList = new ArrayList<>();
        sampleList.add(new Movie(1, "Sample 1", 2000));
        sampleList.add(new Movie(2, "Sample 2", 2001));
        sampleList.add(new Movie(3, "Sample 3", 2002));
        sampleList.forEach(m -> server.getMovieStore().addMovie(m));

        ResponseRecord rec = RequestRunner.runGetRequest(
                URI.create(BASE + "/movies?year=2000"),
                client);

        assertEquals(200, rec.statusCode());

        Movie[] controlList = {sampleList.get(0)};
        List<Movie> targetMovies = new Gson().fromJson(rec.responseBody(), new ListOfMoviesTypeToken().getType());

        assertArrayEquals(controlList, targetMovies.toArray(new Movie[0]));
    }


}