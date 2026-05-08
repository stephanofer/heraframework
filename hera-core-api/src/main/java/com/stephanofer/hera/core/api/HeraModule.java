package com.stephanofer.hera.core.api;

public interface HeraModule {

    ModuleDescriptor descriptor();

    default void configure(HeraRuntime runtime) throws Exception {
    }

    default void start() throws Exception {
    }

    default void stop() throws Exception {
    }
}
