package ru.practicum.moviehub.http;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class RequestRunner {

    public static ResponseRecord runGetRequest(URI url, HttpClient client) throws Exception {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .header("Content-Type", "application/json; charset=UTF-8")
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        return new ResponseRecord(response.statusCode(),
                response.headers().firstValue("Content-Type").orElse(""),
                response.body());
    }

    public static ResponseRecord runPostRequest(URI url, HttpClient client, String body,
                                                boolean needContentHeader) throws Exception {

        HttpRequest request;

        if (needContentHeader) {
            request = HttpRequest.newBuilder()
                    .uri(url)
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .version(HttpClient.Version.HTTP_1_1)
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();
        } else {
            request = HttpRequest.newBuilder()
                    .uri(url)
                    .version(HttpClient.Version.HTTP_1_1)
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();
        }

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        return new ResponseRecord(response.statusCode(),
                response.headers().firstValue("Content-Type").orElse(""),
                response.body());
    }

    public static ResponseRecord runDeleteRequest(URI url, HttpClient client) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .header("Content-Type", "application/json; charset=UTF-8")
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return new ResponseRecord(response.statusCode(), "", "");
    }
}

