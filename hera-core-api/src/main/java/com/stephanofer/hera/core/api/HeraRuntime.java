package com.stephanofer.hera.core.api;

import java.util.Collection;
import java.util.Optional;

public interface HeraRuntime extends AutoCloseable {

    Collection<HeraModule> modules();

    Optional<HeraModule> module(String id);

    default HeraModule requireModule(String id) {
        return module(id).orElseThrow(() -> new IllegalArgumentException("Unknown module id: " + id));
    }

    @Override
    void close();
}
