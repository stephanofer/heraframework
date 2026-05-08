package com.stephanofer.hera.core.runtime;

import com.stephanofer.hera.core.api.HeraModule;
import com.stephanofer.hera.core.api.HeraRuntime;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class DefaultHeraRuntime implements HeraRuntime {

    private final Map<String, HeraModule> modulesById;
    private boolean closed;

    DefaultHeraRuntime(Collection<HeraModule> modules) {
        this.modulesById = new LinkedHashMap<>();
        for (HeraModule module : modules) {
            this.modulesById.put(module.descriptor().id(), module);
        }
    }

    @Override
    public Collection<HeraModule> modules() {
        return this.modulesById.values().stream().toList();
    }

    @Override
    public Optional<HeraModule> module(String id) {
        return Optional.ofNullable(this.modulesById.get(id));
    }

    @Override
    public void close() {
        if (this.closed) {
            return;
        }

        this.closed = true;
        RuntimeShutdown.stopInReverse(this.modulesById.values().stream().toList());
    }
}
