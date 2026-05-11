package com.stephanofer.hera.config.internal;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import org.bukkit.plugin.java.JavaPlugin;

public final class ConfigDocumentLoader {

    private ConfigDocumentLoader() {
    }

    public static LoadedDocument load(JavaPlugin plugin, LoadRequest request) throws IOException {
        Objects.requireNonNull(plugin, "plugin");
        Objects.requireNonNull(request, "request");

        Path filePath = request.filePath();
        Path parent = filePath.getParent();
        if (parent != null && request.createIfMissing()) {
            Files.createDirectories(parent);
        }

        boolean existed = Files.exists(filePath);
        byte[] originalBytes = existed ? Files.readAllBytes(filePath) : null;

        GeneralSettings generalSettings = buildGeneralSettings(request.generalSettingsCustomizer());
        LoaderSettings loaderSettings = buildLoaderSettings(request.createIfMissing(), request.allowDuplicateKeys(), request.loaderSettingsCustomizer());
        DumperSettings dumperSettings = buildDumperSettings(request.dumperSettingsCustomizer());
        UpdaterSettings updaterSettings = buildUpdaterSettings(request.keepAll(), request.versionRoute(), request.updaterSettingsCustomizer());

        List<String> messages = new ArrayList<>();
        if (!existed) {
            messages.add("Created missing file");
        }

        try (InputStream defaultsStream = openDefaults(plugin, request.resourcePath())) {
            YamlDocument document = defaultsStream != null
                ? YamlDocument.create(filePath.toFile(), defaultsStream, generalSettings, loaderSettings, dumperSettings, updaterSettings)
                : YamlDocument.create(filePath.toFile(), generalSettings, loaderSettings, dumperSettings, updaterSettings);

            boolean updated = false;
            boolean backedUp = false;
            String beforeUpdate = document.dump();

            if (request.mergeDefaults() && document.getDefaults() != null) {
                document.update(updaterSettings);
                String afterUpdate = document.dump();
                updated = !beforeUpdate.equals(afterUpdate);
                if (updated) {
                    if (existed && request.backupOnUpdate() && originalBytes != null) {
                        createBackup(filePath, originalBytes);
                        backedUp = true;
                        messages.add("Created backup before update");
                    }
                    AtomicFileWriter.writeString(filePath, afterUpdate);
                    messages.add("Merged defaults into existing file");
                }
            }

            return new LoadedDocument(document, !existed, !existed || updated, updated, backedUp, messages);
        }
    }

    private static GeneralSettings buildGeneralSettings(Consumer<GeneralSettings.Builder> customizer) {
        GeneralSettings.Builder builder = GeneralSettings.builder().setUseDefaults(false);
        customizer.accept(builder);
        return builder.build();
    }

    private static LoaderSettings buildLoaderSettings(boolean createIfMissing, boolean allowDuplicateKeys, Consumer<LoaderSettings.Builder> customizer) {
        LoaderSettings.Builder builder = LoaderSettings.builder()
            .setCreateFileIfAbsent(createIfMissing)
            .setAutoUpdate(false)
            .setDetailedErrors(true)
            .setAllowDuplicateKeys(allowDuplicateKeys);
        customizer.accept(builder);
        return builder.build();
    }

    private static DumperSettings buildDumperSettings(Consumer<DumperSettings.Builder> customizer) {
        DumperSettings.Builder builder = DumperSettings.builder();
        customizer.accept(builder);
        return builder.build();
    }

    private static UpdaterSettings buildUpdaterSettings(boolean keepAll, String versionRoute, Consumer<UpdaterSettings.Builder> customizer) {
        UpdaterSettings.Builder builder = UpdaterSettings.builder()
            .setAutoSave(false)
            .setKeepAll(keepAll);
        if (versionRoute != null && !versionRoute.isBlank()) {
            builder.setVersioning(new BasicVersioning(versionRoute));
        }
        customizer.accept(builder);
        return builder.build();
    }

    private static InputStream openDefaults(JavaPlugin plugin, String resourcePath) {
        String normalized = ConfigPathSupport.normalizeResourcePath(resourcePath);
        if (normalized == null || normalized.isBlank()) {
            return null;
        }

        InputStream stream = plugin.getResource(normalized);
        if (stream == null) {
            throw new IllegalStateException("Missing bundled resource: " + normalized);
        }
        return stream;
    }

    private static void createBackup(Path filePath, byte[] originalBytes) throws IOException {
        String timestamp = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(java.time.ZoneOffset.UTC).format(Instant.now());
        Path backup = filePath.resolveSibling(filePath.getFileName() + ".bak." + timestamp);
        Files.write(backup, originalBytes);
    }

    public record LoadRequest(
        Path filePath,
        String resourcePath,
        boolean createIfMissing,
        boolean mergeDefaults,
        boolean backupOnUpdate,
        boolean allowDuplicateKeys,
        boolean keepAll,
        String versionRoute,
        Consumer<GeneralSettings.Builder> generalSettingsCustomizer,
        Consumer<LoaderSettings.Builder> loaderSettingsCustomizer,
        Consumer<DumperSettings.Builder> dumperSettingsCustomizer,
        Consumer<UpdaterSettings.Builder> updaterSettingsCustomizer
    ) {
    }

    public record LoadedDocument(
        YamlDocument document,
        boolean created,
        boolean changed,
        boolean updated,
        boolean backedUp,
        List<String> messages
    ) {
    }
}
