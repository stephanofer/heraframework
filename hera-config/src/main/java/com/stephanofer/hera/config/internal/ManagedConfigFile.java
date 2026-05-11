package com.stephanofer.hera.config.internal;

import com.stephanofer.hera.config.ConfigCodec;
import com.stephanofer.hera.config.ConfigException;
import com.stephanofer.hera.config.ConfigFileHandle;
import com.stephanofer.hera.config.ConfigFileSpec;
import com.stephanofer.hera.config.ConfigReloadReport;
import com.stephanofer.hera.config.ConfigResourceReport;
import com.stephanofer.hera.config.ConfigSaveReport;
import com.stephanofer.hera.config.ConfigValidationResult;
import com.stephanofer.hera.config.RawYamlHandle;
import dev.dejvokep.boostedyaml.YamlDocument;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;

public final class ManagedConfigFile<T> implements ConfigFileHandle<T> {

    private final JavaPlugin plugin;
    private final ConfigFileSpec<T> spec;
    private final ExecutorService executor;
    private final Logger logger;
    private final Path filePath;
    private final Object monitor = new Object();
    private volatile State<T> state;

    public ManagedConfigFile(JavaPlugin plugin, ConfigFileSpec<T> spec, ExecutorService executor) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.spec = Objects.requireNonNull(spec, "spec");
        this.executor = Objects.requireNonNull(executor, "executor");
        this.logger = plugin.getLogger();
        this.filePath = ConfigPathSupport.resolveInside(plugin.getDataFolder().toPath(), spec.path());
    }

    public ConfigResourceReport initialize() {
        ConfigReloadReport report = reloadNow();
        ConfigResourceReport resource = report.resources().getFirst();
        if (!resource.success() && spec.failFast()) {
            throw new ConfigException("Failed to initialize config file '" + spec.id() + "'", resource.error());
        }
        return resource;
    }

    @Override
    public String id() {
        return spec.id();
    }

    @Override
    public Path filePath() {
        return filePath;
    }

    @Override
    public Class<T> type() {
        return spec.type();
    }

    @Override
    public T snapshot() {
        State<T> current = this.state;
        if (current == null) {
            throw new ConfigException("Config file not loaded yet: " + spec.id());
        }
        return current.snapshot();
    }

    @Override
    public <R> R read(Function<YamlDocument, R> reader) {
        Objects.requireNonNull(reader, "reader");
        State<T> current = this.state;
        if (current == null) {
            throw new ConfigException("Config file not loaded yet: " + spec.id());
        }
        synchronized (monitor) {
            return reader.apply(current.document());
        }
    }

    @Override
    public ConfigSaveReport edit(Consumer<YamlDocument> editor) {
        Objects.requireNonNull(editor, "editor");
        synchronized (monitor) {
            try {
                ConfigDocumentLoader.LoadedDocument loaded = loadDocument();
                YamlDocument working = loaded.document();
                String before = working.dump();
                editor.accept(working);
                Parsed<T> parsed = parse(working);
                String after = working.dump();
                boolean changed = !before.equals(after);
                if (changed) {
                    AtomicFileWriter.writeString(filePath, after);
                }
                this.state = new State<>(working, parsed.snapshot(), parsed.validation());
                return new ConfigSaveReport(spec.id(), filePath, true, loaded.created(), changed, List.of(changed ? "Saved edited document" : "No changes detected"), null);
            } catch (Exception exception) {
                return new ConfigSaveReport(spec.id(), filePath, false, false, false, List.of("Edit failed"), exception);
            }
        }
    }

    @Override
    public CompletableFuture<ConfigSaveReport> editAsync(Consumer<YamlDocument> editor) {
        return CompletableFuture.supplyAsync(() -> edit(editor), executor);
    }

    @Override
    public ConfigReloadReport reloadNow() {
        synchronized (monitor) {
            try {
                ConfigDocumentLoader.LoadedDocument loaded = loadDocument();
                Parsed<T> parsed = parse(loaded.document());
                this.state = new State<>(loaded.document(), parsed.snapshot(), parsed.validation());
                ConfigResourceReport report = new ConfigResourceReport(spec.id(), filePath, true, loaded.created(), loaded.changed(), loaded.updated(), loaded.backedUp(), loaded.messages(), null);
                logReport(report);
                return ConfigReloadReport.single(report);
            } catch (Exception exception) {
                ConfigResourceReport report = new ConfigResourceReport(spec.id(), filePath, false, false, false, false, false, List.of("Reload failed"), exception);
                logReport(report);
                return ConfigReloadReport.single(report);
            }
        }
    }

    @Override
    public CompletableFuture<ConfigReloadReport> reload() {
        return CompletableFuture.supplyAsync(this::reloadNow, executor);
    }

    private ConfigDocumentLoader.LoadedDocument loadDocument() throws Exception {
        return ConfigDocumentLoader.load(plugin, new ConfigDocumentLoader.LoadRequest(
            filePath,
            spec.resourcePath(),
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

    private Parsed<T> parse(YamlDocument document) throws Exception {
        ConfigCodec<T> codec = spec.codec();
        T snapshot = codec.decode(document);
        ConfigValidationResult validation = spec.validator().validate(snapshot, document);
        if (!validation.isValid()) {
            throw new ConfigException("Validation failed for config '" + spec.id() + "': " + validation.violations());
        }
        return new Parsed<>(snapshot, validation);
    }

    private void logReport(ConfigResourceReport report) {
        if (!report.success()) {
            logger.severe("[hera-config] Failed to load '" + report.id() + "' from " + report.path() + ": " + report.error().getMessage());
            return;
        }

        List<String> messages = new ArrayList<>(report.messages());
        if (report.updated()) {
            messages.add("Configuration updated safely");
        }
        if (!messages.isEmpty()) {
            logger.info("[hera-config] " + report.id() + " -> " + String.join("; ", messages));
        }
    }

    private record Parsed<T>(T snapshot, ConfigValidationResult validation) {
    }

    private record State<T>(YamlDocument document, T snapshot, ConfigValidationResult validation) {
    }
}
