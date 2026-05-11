package com.stephanofer.hera.config;

public interface ConfigFileHandle<T> extends RawYamlHandle {

    Class<T> type();

    T snapshot();
}
