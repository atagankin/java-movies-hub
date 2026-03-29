package ru.practicum.moviehub.model;

public class Sequence {
    private static int currentValue = 0;

    public static int nextInt() {
        currentValue++;
        return currentValue;
    }

    public static int curInt() {
        return currentValue;
    }
}
