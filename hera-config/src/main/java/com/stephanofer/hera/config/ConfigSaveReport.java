package com.stephanofer.hera.config;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public record ConfigSaveReport(
    String id,
    Path path,
    boolean success,
    boolean created,
    boolean changed,
    List<String> messages,
    Throwable error
) {

    public ConfigSaveReport {
        id = Objects.requireNonNull(id, "id");
        path = Objects.requireNonNull(path, "path");
        messages = List.copyOf(Objects.requireNonNull(messages, "messages"));
    }
}
