package com.stephanofer.hera.config;

import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public final class ConfigDirectorySpec<T> {

    private final String id;
    private final String directoryPath;
    private final Class<T> type;
    private final ConfigCodec<T> codec;
    private final ConfigValidator<T> validator;
    private final Predicate<Path> fileFilter;
    private final Function<Path, String> defaultResourceResolver;
    private final boolean createIfMissing;
    private final boolean mergeDefaults;
    private final boolean backupOnUpdate;
    private final boolean allowDuplicateKeys;
    private final boolean keepAll;
    private final boolean allowCreateOnEdit;
    private final boolean failOnInvalidEntry;
    private final String versionRoute;
    private final Consumer<GeneralSettings.Builder> generalSettingsCustomizer;
    private final Consumer<LoaderSettings.Builder> loaderSettingsCustomizer;
    private final Consumer<DumperSettings.Builder> dumperSettingsCustomizer;
    private final Consumer<UpdaterSettings.Builder> updaterSettingsCustomizer;

    private ConfigDirectorySpec(Builder<T> builder) {
        this.id = builder.id;
        this.directoryPath = builder.directoryPath;
        this.type = builder.type;
        this.codec = builder.codec;
        this.validator = builder.validator;
        this.fileFilter = builder.fileFilter;
        this.defaultResourceResolver = builder.defaultResourceResolver;
        this.createIfMissing = builder.createIfMissing;
        this.mergeDefaults = builder.mergeDefaults;
        this.backupOnUpdate = builder.backupOnUpdate;
        this.allowDuplicateKeys = builder.allowDuplicateKeys;
        this.keepAll = builder.keepAll;
        this.allowCreateOnEdit = builder.allowCreateOnEdit;
        this.failOnInvalidEntry = builder.failOnInvalidEntry;
        this.versionRoute = builder.versionRoute;
        this.generalSettingsCustomizer = builder.generalSettingsCustomizer;
        this.loaderSettingsCustomizer = builder.loaderSettingsCustomizer;
        this.dumperSettingsCustomizer = builder.dumperSettingsCustomizer;
        this.updaterSettingsCustomizer = builder.updaterSettingsCustomizer;
    }

    public String id() {
        return id;
    }

    public String directoryPath() {
        return directoryPath;
    }

    public Class<T> type() {
        return type;
    }

    public ConfigCodec<T> codec() {
        return codec;
    }

    public ConfigValidator<T> validator() {
        return validator;
    }

    public Predicate<Path> fileFilter() {
        return fileFilter;
    }

    public Function<Path, String> defaultResourceResolver() {
        return defaultResourceResolver;
    }

    public boolean createIfMissing() {
        return createIfMissing;
    }

    public boolean mergeDefaults() {
        return mergeDefaults;
    }

    public boolean backupOnUpdate() {
        return backupOnUpdate;
    }

    public boolean allowDuplicateKeys() {
        return allowDuplicateKeys;
    }

    public boolean keepAll() {
        return keepAll;
    }

    public boolean allowCreateOnEdit() {
        return allowCreateOnEdit;
    }

    public boolean failOnInvalidEntry() {
        return failOnInvalidEntry;
    }

    public String versionRoute() {
        return versionRoute;
    }

    public Consumer<GeneralSettings.Builder> generalSettingsCustomizer() {
        return generalSettingsCustomizer;
    }

    public Consumer<LoaderSettings.Builder> loaderSettingsCustomizer() {
        return loaderSettingsCustomizer;
    }

    public Consumer<DumperSettings.Builder> dumperSettingsCustomizer() {
        return dumperSettingsCustomizer;
    }

    public Consumer<UpdaterSettings.Builder> updaterSettingsCustomizer() {
        return updaterSettingsCustomizer;
    }

    public static <T> Builder<T> builder(String id, String directoryPath, Class<T> type, ConfigCodec<T> codec) {
        return new Builder<>(id, directoryPath, type, codec);
    }

    public static final class Builder<T> {

        private final String id;
        private final String directoryPath;
        private final Class<T> type;
        private final ConfigCodec<T> codec;
        private ConfigValidator<T> validator = ConfigValidator.noop();
        private Predicate<Path> fileFilter = path -> path.getFileName().toString().endsWith(".yml");
        private Function<Path, String> defaultResourceResolver = relativePath -> null;
        private boolean createIfMissing = true;
        private boolean mergeDefaults = true;
        private boolean backupOnUpdate = true;
        private boolean allowDuplicateKeys = false;
        private boolean keepAll = true;
        private boolean allowCreateOnEdit = true;
        private boolean failOnInvalidEntry = false;
        private String versionRoute;
        private Consumer<GeneralSettings.Builder> generalSettingsCustomizer = builder -> {};
        private Consumer<LoaderSettings.Builder> loaderSettingsCustomizer = builder -> {};
        private Consumer<DumperSettings.Builder> dumperSettingsCustomizer = builder -> {};
        private Consumer<UpdaterSettings.Builder> updaterSettingsCustomizer = builder -> {};

        private Builder(String id, String directoryPath, Class<T> type, ConfigCodec<T> codec) {
            this.id = Objects.requireNonNull(id, "id");
            this.directoryPath = Objects.requireNonNull(directoryPath, "directoryPath");
            this.type = Objects.requireNonNull(type, "type");
            this.codec = Objects.requireNonNull(codec, "codec");
        }

        public Builder<T> validator(ConfigValidator<T> validator) {
            this.validator = Objects.requireNonNull(validator, "validator");
            return this;
        }

        public Builder<T> fileFilter(Predicate<Path> fileFilter) {
            this.fileFilter = Objects.requireNonNull(fileFilter, "fileFilter");
            return this;
        }

        public Builder<T> defaultResourceResolver(Function<Path, String> defaultResourceResolver) {
            this.defaultResourceResolver = Objects.requireNonNull(defaultResourceResolver, "defaultResourceResolver");
            return this;
        }

        public Builder<T> createIfMissing(boolean createIfMissing) {
            this.createIfMissing = createIfMissing;
            return this;
        }

        public Builder<T> mergeDefaults(boolean mergeDefaults) {
            this.mergeDefaults = mergeDefaults;
            return this;
        }

        public Builder<T> backupOnUpdate(boolean backupOnUpdate) {
            this.backupOnUpdate = backupOnUpdate;
            return this;
        }

        public Builder<T> allowDuplicateKeys(boolean allowDuplicateKeys) {
            this.allowDuplicateKeys = allowDuplicateKeys;
            return this;
        }

        public Builder<T> keepAll(boolean keepAll) {
            this.keepAll = keepAll;
            return this;
        }

        public Builder<T> allowCreateOnEdit(boolean allowCreateOnEdit) {
            this.allowCreateOnEdit = allowCreateOnEdit;
            return this;
        }

        public Builder<T> failOnInvalidEntry(boolean failOnInvalidEntry) {
            this.failOnInvalidEntry = failOnInvalidEntry;
            return this;
        }

        public Builder<T> versionRoute(String versionRoute) {
            this.versionRoute = versionRoute;
            return this;
        }

        public Builder<T> customizeGeneralSettings(Consumer<GeneralSettings.Builder> customizer) {
            this.generalSettingsCustomizer = this.generalSettingsCustomizer.andThen(Objects.requireNonNull(customizer, "customizer"));
            return this;
        }

        public Builder<T> customizeLoaderSettings(Consumer<LoaderSettings.Builder> customizer) {
            this.loaderSettingsCustomizer = this.loaderSettingsCustomizer.andThen(Objects.requireNonNull(customizer, "customizer"));
            return this;
        }

        public Builder<T> customizeDumperSettings(Consumer<DumperSettings.Builder> customizer) {
            this.dumperSettingsCustomizer = this.dumperSettingsCustomizer.andThen(Objects.requireNonNull(customizer, "customizer"));
            return this;
        }

        public Builder<T> customizeUpdaterSettings(Consumer<UpdaterSettings.Builder> customizer) {
            this.updaterSettingsCustomizer = this.updaterSettingsCustomizer.andThen(Objects.requireNonNull(customizer, "customizer"));
            return this;
        }

        public ConfigDirectorySpec<T> build() {
            return new ConfigDirectorySpec<>(this);
        }
    }
}
