package com.stephanofer.hera.config;

import java.util.Objects;

public record ConfigViolation(String path, String message) {

    public ConfigViolation {
        path = Objects.requireNonNullElse(path, "<root>");
        message = Objects.requireNonNull(message, "message");
    }
}
