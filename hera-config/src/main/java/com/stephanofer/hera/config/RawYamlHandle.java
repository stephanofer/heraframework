package com.stephanofer.hera.config;

import dev.dejvokep.boostedyaml.YamlDocument;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public interface RawYamlHandle {

    String id();

    Path filePath();

    <R> R read(Function<YamlDocument, R> reader);

    ConfigSaveReport edit(Consumer<YamlDocument> editor);

    CompletableFuture<ConfigSaveReport> editAsync(Consumer<YamlDocument> editor);

    ConfigReloadReport reloadNow();

    CompletableFuture<ConfigReloadReport> reload();
}
