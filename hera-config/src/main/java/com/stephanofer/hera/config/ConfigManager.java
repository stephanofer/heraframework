package com.stephanofer.hera.config;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface ConfigManager extends AutoCloseable {

    Set<String> resourceIds();

    <T> ConfigFileHandle<T> file(String id, Class<T> type);

    <T> ConfigDirectoryHandle<T> directory(String id, Class<T> type);

    RawYamlHandle rawFile(String id);

    ConfigDirectoryHandle<?> rawDirectory(String id);

    ConfigReloadReport reloadNow(String id);

    CompletableFuture<ConfigReloadReport> reload(String id);

    ConfigReloadReport reloadAllNow();

    CompletableFuture<ConfigReloadReport> reloadAll();

    @Override
    void close();
}
