package com.stephanofer.hera.config;

import dev.dejvokep.boostedyaml.YamlDocument;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface ConfigDirectoryHandle<T> {

    String id();

    Path directoryPath();

    Class<T> type();

    Set<String> keys();

    Map<String, T> snapshots();

    Optional<T> snapshot(String key);

    Optional<RawYamlHandle> raw(String key);

    ConfigSaveReport edit(String key, Consumer<YamlDocument> editor);

    CompletableFuture<ConfigSaveReport> editAsync(String key, Consumer<YamlDocument> editor);

    ConfigReloadReport reloadNow();

    CompletableFuture<ConfigReloadReport> reload();
}
