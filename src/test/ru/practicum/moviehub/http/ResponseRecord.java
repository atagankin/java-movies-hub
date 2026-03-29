package ru.practicum.moviehub.http;

public record ResponseRecord(
        int statusCode,
        String paramContentType,
        String responseBody
) {}
