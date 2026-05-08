package com.stephanofer.hera.core.runtime;

import com.stephanofer.hera.core.api.HeraModule;
import com.stephanofer.hera.core.api.HeraRuntime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class HeraRuntimeBuilder {

    private final Map<String, HeraModule> modulesById = new LinkedHashMap<>();

    public HeraRuntimeBuilder register(HeraModule module) {
        Objects.requireNonNull(module, "module");

        String moduleId = module.descriptor().id();
        HeraModule previous = this.modulesById.putIfAbsent(moduleId, module);
        if (previous != null) {
            throw new ModuleGraphException("Duplicated module id: " + moduleId);
        }

        return this;
    }

    public HeraRuntime build() {
        ModuleSorter sorter = new ModuleSorter(this.modulesById);
        List<HeraModule> sortedModules = sorter.sort();
        DefaultHeraRuntime runtime = new DefaultHeraRuntime(sortedModules);
        List<HeraModule> configuredModules = new ArrayList<>();

        try {
            for (HeraModule module : sortedModules) {
                module.configure(runtime);
                configuredModules.add(module);
            }

            for (HeraModule module : sortedModules) {
                module.start();
            }

            return runtime;
        } catch (Exception exception) {
            RuntimeShutdown.stopInReverse(configuredModules);
            throw new ModuleStartupException("Failed to bootstrap Hera runtime", exception);
        }
    }
}
