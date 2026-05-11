package com.stephanofer.hera.config;

import dev.dejvokep.boostedyaml.YamlDocument;

@FunctionalInterface
public interface ConfigCodec<T> {

    T decode(YamlDocument document) throws Exception;
}
