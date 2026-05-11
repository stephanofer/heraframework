package com.stephanofer.hera.config;

import com.stephanofer.hera.config.internal.ManagedConfigDirectory;
import com.stephanofer.hera.config.internal.ManagedConfigFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import org.bukkit.plugin.java.JavaPlugin;

public final class HeraConfigModule implements ConfigManager {

    private final ExecutorService executor;
    private final Map<String, ManagedConfigFile<?>> files;
    private final Map<String, ManagedConfigDirectory<?>> directories;

    private HeraConfigModule(JavaPlugin plugin, ExecutorService executor, Map<String, ManagedConfigFile<?>> files, Map<String, ManagedConfigDirectory<?>> directories) {
        this.executor = executor;
        this.files = Collections.unmodifiableMap(new LinkedHashMap<>(files));
        this.directories = Collections.unmodifiableMap(new LinkedHashMap<>(directories));
    }

    public static Builder builder(JavaPlugin plugin) {
        return new Builder(plugin);
    }

    @Override
    public Set<String> resourceIds() {
        Set<String> ids = new LinkedHashSet<>();
        ids.addAll(files.keySet());
        ids.addAll(directories.keySet());
        return Collections.unmodifiableSet(ids);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> ConfigFileHandle<T> file(String id, Class<T> type) {
        ManagedConfigFile<?> handle = files.get(id);
        if (handle == null) {
            throw new IllegalArgumentException("Unknown config file id: " + id);
        }
        if (!type.isAssignableFrom(handle.type())) {
            throw new IllegalArgumentException("Config file '" + id + "' is registered as " + handle.type().getName() + ", not " + type.getName());
        }
        return (ConfigFileHandle<T>) handle;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> ConfigDirectoryHandle<T> directory(String id, Class<T> type) {
        ManagedConfigDirectory<?> handle = directories.get(id);
        if (handle == null) {
            throw new IllegalArgumentException("Unknown config directory id: " + id);
        }
        if (!type.isAssignableFrom(handle.type())) {
            throw new IllegalArgumentException("Config directory '" + id + "' is registered as " + handle.type().getName() + ", not " + type.getName());
        }
        return (ConfigDirectoryHandle<T>) handle;
    }

    @Override
    public RawYamlHandle rawFile(String id) {
        ManagedConfigFile<?> handle = files.get(id);
        if (handle == null) {
            throw new IllegalArgumentException("Unknown config file id: " + id);
        }
        return handle;
    }

    @Override
    public ConfigDirectoryHandle<?> rawDirectory(String id) {
        ManagedConfigDirectory<?> handle = directories.get(id);
        if (handle == null) {
            throw new IllegalArgumentException("Unknown config directory id: " + id);
        }
        return handle;
    }

    @Override
    public ConfigReloadReport reloadNow(String id) {
        ManagedConfigFile<?> file = files.get(id);
        if (file != null) {
            return file.reloadNow();
        }
        ManagedConfigDirectory<?> directory = directories.get(id);
        if (directory != null) {
            return directory.reloadNow();
        }
        throw new IllegalArgumentException("Unknown config resource id: " + id);
    }

    @Override
    public CompletableFuture<ConfigReloadReport> reload(String id) {
        return CompletableFuture.supplyAsync(() -> reloadNow(id), executor);
    }

    @Override
    public ConfigReloadReport reloadAllNow() {
        List<ConfigResourceReport> reports = new ArrayList<>();
        files.values().forEach(handle -> reports.addAll(handle.reloadNow().resources()));
        directories.values().forEach(handle -> reports.addAll(handle.reloadNow().resources()));
        return new ConfigReloadReport(reports);
    }

    @Override
    public CompletableFuture<ConfigReloadReport> reloadAll() {
        return CompletableFuture.supplyAsync(this::reloadAllNow, executor);
    }

    @Override
    public void close() {
        executor.shutdown();
    }

    public static final class Builder {

        private final JavaPlugin plugin;
        private final Map<String, ConfigFileSpec<?>> fileSpecs = new LinkedHashMap<>();
        private final Map<String, ConfigDirectorySpec<?>> directorySpecs = new LinkedHashMap<>();
        private String threadName = "hera-config-io";

        private Builder(JavaPlugin plugin) {
            this.plugin = Objects.requireNonNull(plugin, "plugin");
        }

        public Builder ioThreadName(String threadName) {
            this.threadName = Objects.requireNonNull(threadName, "threadName");
            return this;
        }

        public <T> Builder file(ConfigFileSpec<T> spec) {
            ensureIdAvailable(spec.id());
            register(spec.id(), spec, fileSpecs, "file");
            return this;
        }

        public <T> Builder directory(ConfigDirectorySpec<T> spec) {
            ensureIdAvailable(spec.id());
            register(spec.id(), spec, directorySpecs, "directory");
            return this;
        }

        public Builder rawFile(String id, String path, String resourcePath) {
            return file(ConfigFileSpec.builder(id, path, Void.class, document -> null)
                .resourcePath(resourcePath)
                .build());
        }

        public Builder rawDirectory(String id, String directoryPath) {
            return directory(ConfigDirectorySpec.builder(id, directoryPath, Void.class, document -> null).build());
        }

        public HeraConfigModule build() {
            ExecutorService executor = Executors.newSingleThreadExecutor(new ConfigThreadFactory(threadName));
            Map<String, ManagedConfigFile<?>> files = new LinkedHashMap<>();
            Map<String, ManagedConfigDirectory<?>> directories = new LinkedHashMap<>();

            for (ConfigFileSpec<?> spec : fileSpecs.values()) {
                ManagedConfigFile<?> handle = new ManagedConfigFile<>(plugin, spec, executor);
                handle.initialize();
                files.put(spec.id(), handle);
            }

            for (ConfigDirectorySpec<?> spec : directorySpecs.values()) {
                ManagedConfigDirectory<?> handle = new ManagedConfigDirectory<>(plugin, spec, executor);
                handle.initialize();
                directories.put(spec.id(), handle);
            }

            return new HeraConfigModule(plugin, executor, files, directories);
        }

        private static <T> void register(String id, T spec, Map<String, T> target, String kind) {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(spec, "spec");
            if (target.putIfAbsent(id, spec) != null) {
                throw new IllegalArgumentException("Duplicate config " + kind + " id: " + id);
            }
        }

        private void ensureIdAvailable(String id) {
            if (fileSpecs.containsKey(id) || directorySpecs.containsKey(id)) {
                throw new IllegalArgumentException("Duplicate config resource id: " + id);
            }
        }
    }

    private static final class ConfigThreadFactory implements ThreadFactory {

        private final String name;

        private ConfigThreadFactory(String name) {
            this.name = name;
        }

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, name);
            thread.setDaemon(true);
            return thread;
        }
    }
}
