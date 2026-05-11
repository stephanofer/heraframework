package com.stephanofer.hera.config.internal;

import com.stephanofer.hera.config.ConfigCodec;
import com.stephanofer.hera.config.ConfigDirectoryHandle;
import com.stephanofer.hera.config.ConfigDirectorySpec;
import com.stephanofer.hera.config.ConfigException;
import com.stephanofer.hera.config.ConfigReloadReport;
import com.stephanofer.hera.config.ConfigResourceReport;
import com.stephanofer.hera.config.ConfigSaveReport;
import com.stephanofer.hera.config.ConfigValidationResult;
import com.stephanofer.hera.config.RawYamlHandle;
import dev.dejvokep.boostedyaml.YamlDocument;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.bukkit.plugin.java.JavaPlugin;

public final class ManagedConfigDirectory<T> implements ConfigDirectoryHandle<T> {

    private final JavaPlugin plugin;
    private final ConfigDirectorySpec<T> spec;
    private final ExecutorService executor;
    private final Logger logger;
    private final Path directoryPath;
    private final Object monitor = new Object();
    private volatile Map<String, EntryState<T>> states = Map.of();

    public ManagedConfigDirectory(JavaPlugin plugin, ConfigDirectorySpec<T> spec, ExecutorService executor) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.spec = Objects.requireNonNull(spec, "spec");
        this.executor = Objects.requireNonNull(executor, "executor");
        this.logger = plugin.getLogger();
        this.directoryPath = ConfigPathSupport.resolveInside(plugin.getDataFolder().toPath(), spec.directoryPath());
    }

    public ConfigReloadReport initialize() {
        ConfigReloadReport report = reloadNow();
        if (!report.success() && spec.failOnInvalidEntry()) {
            throw new ConfigException("Failed to initialize config directory '" + spec.id() + "'");
        }
        return report;
    }

    @Override
    public String id() {
        return spec.id();
    }

    @Override
    public Path directoryPath() {
        return directoryPath;
    }

    @Override
    public Class<T> type() {
        return spec.type();
    }

    @Override
    public Set<String> keys() {
        return new LinkedHashSet<>(states.keySet());
    }

    @Override
    public Map<String, T> snapshots() {
        Map<String, T> snapshots = new LinkedHashMap<>();
        states.forEach((key, value) -> snapshots.put(key, value.snapshot()));
        return Collections.unmodifiableMap(snapshots);
    }

    @Override
    public Optional<T> snapshot(String key) {
        EntryState<T> state = states.get(normalizeKey(key));
        return state == null ? Optional.empty() : Optional.of(state.snapshot());
    }

    @Override
    public Optional<RawYamlHandle> raw(String key) {
        String normalizedKey = normalizeKey(key);
        EntryState<T> state = states.get(normalizedKey);
        return state == null ? Optional.empty() : Optional.of(new DirectoryEntryHandle(normalizedKey));
    }

    @Override
    public ConfigSaveReport edit(String key, Consumer<YamlDocument> editor) {
        Objects.requireNonNull(editor, "editor");
        synchronized (monitor) {
            String normalizedKey = normalizeKey(key);
            Path filePath = ConfigPathSupport.entryPath(directoryPath, normalizedKey);
            try {
                if (!Files.exists(filePath) && !spec.allowCreateOnEdit()) {
                    return new ConfigSaveReport(spec.id() + ":" + normalizedKey, filePath, false, false, false, List.of("Entry does not exist and create-on-edit is disabled"), null);
                }

                ConfigDocumentLoader.LoadedDocument loaded = loadEntry(filePath, normalizedKey);
                YamlDocument working = loaded.document();
                String before = working.dump();
                editor.accept(working);
                Parsed<T> parsed = parse(working, normalizedKey);
                String after = working.dump();
                boolean changed = !before.equals(after);
                if (changed) {
                    AtomicFileWriter.writeString(filePath, after);
                }

                Map<String, EntryState<T>> updatedStates = new LinkedHashMap<>(states);
                updatedStates.put(normalizedKey, new EntryState<>(filePath, working, parsed.snapshot(), parsed.validation()));
                this.states = Collections.unmodifiableMap(new LinkedHashMap<>(updatedStates));
                return new ConfigSaveReport(spec.id() + ":" + normalizedKey, filePath, true, loaded.created(), changed, List.of(changed ? "Saved directory entry" : "No changes detected"), null);
            } catch (Exception exception) {
                return new ConfigSaveReport(spec.id() + ":" + normalizedKey, filePath, false, false, false, List.of("Edit failed"), exception);
            }
        }
    }

    @Override
    public CompletableFuture<ConfigSaveReport> editAsync(String key, Consumer<YamlDocument> editor) {
        return CompletableFuture.supplyAsync(() -> edit(key, editor), executor);
    }

    @Override
    public ConfigReloadReport reloadNow() {
        synchronized (monitor) {
            try {
                Files.createDirectories(directoryPath);
            } catch (IOException exception) {
                return ConfigReloadReport.single(new ConfigResourceReport(spec.id(), directoryPath, false, false, false, false, false, List.of("Failed to create directory"), exception));
            }

            Map<String, EntryState<T>> nextStates = new LinkedHashMap<>();
            Map<String, EntryState<T>> previousStates = this.states;
            List<ConfigResourceReport> reports = new ArrayList<>();

            try (Stream<Path> stream = Files.walk(directoryPath)) {
                stream.filter(Files::isRegularFile)
                    .map(directoryPath::relativize)
                    .filter(spec.fileFilter())
                    .sorted()
                    .forEach(relativePath -> {
                        String entryKey = ConfigPathSupport.entryKey(relativePath);
                        Path absolutePath = directoryPath.resolve(relativePath).normalize();
                        try {
                            ConfigDocumentLoader.LoadedDocument loaded = loadEntry(absolutePath, entryKey);
                            Parsed<T> parsed = parse(loaded.document(), entryKey);
                            nextStates.put(entryKey, new EntryState<>(absolutePath, loaded.document(), parsed.snapshot(), parsed.validation()));
                            reports.add(new ConfigResourceReport(spec.id() + ":" + entryKey, absolutePath, true, loaded.created(), loaded.changed(), loaded.updated(), loaded.backedUp(), loaded.messages(), null));
                        } catch (Exception exception) {
                            EntryState<T> previous = previousStates.get(entryKey);
                            if (previous != null) {
                                nextStates.put(entryKey, previous);
                            }
                            reports.add(new ConfigResourceReport(spec.id() + ":" + entryKey, absolutePath, false, false, false, false, false, List.of(previous != null ? "Reload failed, previous snapshot preserved" : "Reload failed"), exception));
                        }
                    });
            } catch (IOException exception) {
                reports.add(new ConfigResourceReport(spec.id(), directoryPath, false, false, false, false, false, List.of("Directory scan failed"), exception));
            }

            previousStates.keySet().stream()
                .filter(previousKey -> !nextStates.containsKey(previousKey))
                .forEach(previousKey -> reports.add(new ConfigResourceReport(
                    spec.id() + ":" + previousKey,
                    previousStates.get(previousKey).filePath(),
                    true,
                    false,
                    true,
                    false,
                    false,
                    List.of("Entry removed from disk and unpublished"),
                    null
                )));

            this.states = Collections.unmodifiableMap(new LinkedHashMap<>(nextStates));
            reports.forEach(this::logReport);
            return new ConfigReloadReport(reports);
        }
    }

    @Override
    public CompletableFuture<ConfigReloadReport> reload() {
        return CompletableFuture.supplyAsync(this::reloadNow, executor);
    }

    private ConfigDocumentLoader.LoadedDocument loadEntry(Path filePath, String key) throws Exception {
        Path relativePath = directoryPath.relativize(filePath);
        return ConfigDocumentLoader.load(plugin, new ConfigDocumentLoader.LoadRequest(
            filePath,
            spec.defaultResourceResolver().apply(relativePath),
            spec.createIfMissing(),
            spec.mergeDefaults(),
            spec.backupOnUpdate(),
            spec.allowDuplicateKeys(),
            spec.keepAll(),
            spec.versionRoute(),
            spec.generalSettingsCustomizer(),
            spec.loaderSettingsCustomizer(),
            spec.dumperSettingsCustomizer(),
            spec.updaterSettingsCustomizer()
        ));
    }

    private Parsed<T> parse(YamlDocument document, String key) throws Exception {
        ConfigCodec<T> codec = spec.codec();
        T snapshot = codec.decode(document);
        ConfigValidationResult validation = spec.validator().validate(snapshot, document);
        if (!validation.isValid()) {
            throw new ConfigException("Validation failed for directory entry '" + spec.id() + "/" + key + "': " + validation.violations());
        }
        return new Parsed<>(snapshot, validation);
    }

    private void logReport(ConfigResourceReport report) {
        if (!report.success()) {
            logger.severe("[hera-config] Failed to load '" + report.id() + "' from " + report.path() + ": " + report.error().getMessage());
            return;
        }
        if (!report.messages().isEmpty()) {
            logger.info("[hera-config] " + report.id() + " -> " + String.join("; ", report.messages()));
        }
    }

    private String normalizeKey(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Directory entry key cannot be blank");
        }
        String normalized = key.replace('\\', '/');
        return normalized.endsWith(".yml") ? normalized.substring(0, normalized.length() - 4) : normalized;
    }

    private final class DirectoryEntryHandle implements RawYamlHandle {

        private final String key;

        private DirectoryEntryHandle(String key) {
            this.key = key;
        }

        @Override
        public String id() {
            return spec.id() + ":" + key;
        }

        @Override
        public Path filePath() {
            EntryState<T> current = states.get(key);
            return current != null ? current.filePath() : ConfigPathSupport.entryPath(directoryPath, key);
        }

        @Override
        public <R> R read(java.util.function.Function<YamlDocument, R> reader) {
            Objects.requireNonNull(reader, "reader");
            EntryState<T> current = states.get(key);
            if (current == null) {
                throw new ConfigException("Unknown directory entry: " + key);
            }
            synchronized (monitor) {
                return reader.apply(current.document());
            }
        }

        @Override
        public ConfigSaveReport edit(Consumer<YamlDocument> editor) {
            return ManagedConfigDirectory.this.edit(key, editor);
        }

        @Override
        public CompletableFuture<ConfigSaveReport> editAsync(Consumer<YamlDocument> editor) {
            return ManagedConfigDirectory.this.editAsync(key, editor);
        }

        @Override
        public ConfigReloadReport reloadNow() {
            return ManagedConfigDirectory.this.reloadNow();
        }

        @Override
        public CompletableFuture<ConfigReloadReport> reload() {
            return ManagedConfigDirectory.this.reload();
        }
    }

    private record Parsed<T>(T snapshot, ConfigValidationResult validation) {
    }

    private record EntryState<T>(Path filePath, YamlDocument document, T snapshot, ConfigValidationResult validation) {
    }
}
