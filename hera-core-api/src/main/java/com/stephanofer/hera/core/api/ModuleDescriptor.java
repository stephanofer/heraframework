package com.stephanofer.hera.core.api;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public record ModuleDescriptor(String id, Set<ModuleDependency> dependencies, boolean reloadable) {

    public ModuleDescriptor {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(dependencies, "dependencies");

        if (id.isBlank()) {
            throw new IllegalArgumentException("Module id cannot be blank");
        }

        dependencies = Set.copyOf(dependencies);

        final boolean selfDependency = dependencies.stream().map(ModuleDependency::id).anyMatch(id::equals);
        if (selfDependency) {
            throw new IllegalArgumentException("Module cannot depend on itself: " + id);
        }
    }

    public static ModuleDescriptor of(String id) {
        return new ModuleDescriptor(id, Set.of(), false);
    }

    public Set<String> requiredDependencies() {
        return dependencies.stream()
            .filter(ModuleDependency::required)
            .map(ModuleDependency::id)
            .collect(Collectors.toUnmodifiableSet());
    }

    public Set<String> optionalDependencies() {
        return dependencies.stream()
            .filter(dependency -> !dependency.required())
            .map(ModuleDependency::id)
            .collect(Collectors.toUnmodifiableSet());
    }
}
