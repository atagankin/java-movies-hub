package ru.practicum.moviehub.http;

import com.sun.net.httpserver.HttpServer;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.net.InetSocketAddress;

public class MoviesServer {
    private final HttpServer server;
    private final MoviesStore movieStore;

    public MoviesServer(MoviesStore store, int port) {
        try {
            movieStore = store;
            // создайте сервер
            server = HttpServer.create();
            server.bind(new InetSocketAddress(port), 0);
            server.createContext("/movies", new MoviesHandler(movieStore));
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать HTTP-сервер", e);
        }
    }

    public void start() {
        // запустите сервер
        server.start();
        System.out.println("Сервер запущен");
    }

    public void stop() {
        // остановите сервер
        server.stop(0);
        System.out.println("Сервер остановлен");
    }

    public MoviesStore getMovieStore() {
        return movieStore;
    }
}