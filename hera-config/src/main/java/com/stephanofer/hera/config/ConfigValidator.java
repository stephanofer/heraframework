package com.stephanofer.hera.config;

import dev.dejvokep.boostedyaml.YamlDocument;

@FunctionalInterface
public interface ConfigValidator<T> {

    ConfigValidationResult validate(T snapshot, YamlDocument document);

    static <T> ConfigValidator<T> noop() {
        return (snapshot, document) -> ConfigValidationResult.valid();
    }
}
