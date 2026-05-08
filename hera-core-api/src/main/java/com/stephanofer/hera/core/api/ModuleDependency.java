package com.stephanofer.hera.core.api;

import java.util.Objects;

public record ModuleDependency(String id, boolean required) {

    public ModuleDependency {
        Objects.requireNonNull(id, "id");

        if (id.isBlank()) {
            throw new IllegalArgumentException("Module dependency id cannot be blank");
        }
    }

    public static ModuleDependency required(String id) {
        return new ModuleDependency(id, true);
    }

    public static ModuleDependency optional(String id) {
        return new ModuleDependency(id, false);
    }
}
