package com.stephanofer.hera.config;

import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import java.util.Objects;
import java.util.function.Consumer;

public final class ConfigFileSpec<T> {

    private final String id;
    private final String path;
    private final String resourcePath;
    private final Class<T> type;
    private final ConfigCodec<T> codec;
    private final ConfigValidator<T> validator;
    private final boolean createIfMissing;
    private final boolean mergeDefaults;
    private final boolean backupOnUpdate;
    private final boolean allowDuplicateKeys;
    private final boolean keepAll;
    private final String versionRoute;
    private final boolean failFast;
    private final Consumer<GeneralSettings.Builder> generalSettingsCustomizer;
    private final Consumer<LoaderSettings.Builder> loaderSettingsCustomizer;
    private final Consumer<DumperSettings.Builder> dumperSettingsCustomizer;
    private final Consumer<UpdaterSettings.Builder> updaterSettingsCustomizer;

    private ConfigFileSpec(Builder<T> builder) {
        this.id = builder.id;
        this.path = builder.path;
        this.resourcePath = builder.resourcePath;
        this.type = builder.type;
        this.codec = builder.codec;
        this.validator = builder.validator;
        this.createIfMissing = builder.createIfMissing;
        this.mergeDefaults = builder.mergeDefaults;
        this.backupOnUpdate = builder.backupOnUpdate;
        this.allowDuplicateKeys = builder.allowDuplicateKeys;
        this.keepAll = builder.keepAll;
        this.versionRoute = builder.versionRoute;
        this.failFast = builder.failFast;
        this.generalSettingsCustomizer = builder.generalSettingsCustomizer;
        this.loaderSettingsCustomizer = builder.loaderSettingsCustomizer;
        this.dumperSettingsCustomizer = builder.dumperSettingsCustomizer;
        this.updaterSettingsCustomizer = builder.updaterSettingsCustomizer;
    }

    public String id() {
        return id;
    }

    public String path() {
        return path;
    }

    public String resourcePath() {
        return resourcePath;
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

    public String versionRoute() {
        return versionRoute;
    }

    public boolean failFast() {
        return failFast;
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

    public static <T> Builder<T> builder(String id, String path, Class<T> type, ConfigCodec<T> codec) {
        return new Builder<>(id, path, type, codec);
    }

    public static final class Builder<T> {

        private final String id;
        private final String path;
        private final Class<T> type;
        private final ConfigCodec<T> codec;
        private String resourcePath;
        private ConfigValidator<T> validator = ConfigValidator.noop();
        private boolean createIfMissing = true;
        private boolean mergeDefaults = true;
        private boolean backupOnUpdate = true;
        private boolean allowDuplicateKeys = false;
        private boolean keepAll = true;
        private String versionRoute;
        private boolean failFast = true;
        private Consumer<GeneralSettings.Builder> generalSettingsCustomizer = builder -> {};
        private Consumer<LoaderSettings.Builder> loaderSettingsCustomizer = builder -> {};
        private Consumer<DumperSettings.Builder> dumperSettingsCustomizer = builder -> {};
        private Consumer<UpdaterSettings.Builder> updaterSettingsCustomizer = builder -> {};

        private Builder(String id, String path, Class<T> type, ConfigCodec<T> codec) {
            this.id = Objects.requireNonNull(id, "id");
            this.path = Objects.requireNonNull(path, "path");
            this.type = Objects.requireNonNull(type, "type");
            this.codec = Objects.requireNonNull(codec, "codec");
        }

        public Builder<T> resourcePath(String resourcePath) {
            this.resourcePath = resourcePath;
            return this;
        }

        public Builder<T> validator(ConfigValidator<T> validator) {
            this.validator = Objects.requireNonNull(validator, "validator");
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

        public Builder<T> versionRoute(String versionRoute) {
            this.versionRoute = versionRoute;
            return this;
        }

        public Builder<T> failFast(boolean failFast) {
            this.failFast = failFast;
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

        public ConfigFileSpec<T> build() {
            return new ConfigFileSpec<>(this);
        }
    }
}
