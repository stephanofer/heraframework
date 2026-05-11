package com.stephanofer.hera.config.internal;

import java.nio.file.Path;

public final class ConfigPathSupport {

    private ConfigPathSupport() {
    }

    public static Path resolveInside(Path baseDirectory, String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            throw new IllegalArgumentException("Path cannot be blank");
        }

        Path normalizedBase = baseDirectory.toAbsolutePath().normalize();
        Path resolved = normalizedBase.resolve(relativePath.replace('\\', '/')).normalize();
        if (!resolved.startsWith(normalizedBase)) {
            throw new IllegalArgumentException("Path escapes plugin data folder: " + relativePath);
        }
        return resolved;
    }

    public static String normalizeResourcePath(String resourcePath) {
        if (resourcePath == null) {
            return null;
        }

        String normalized = resourcePath.replace('\\', '/');
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }

    public static String entryKey(Path relativeFile) {
        String normalized = relativeFile.toString().replace('\\', '/');
        return normalized.endsWith(".yml") ? normalized.substring(0, normalized.length() - 4) : normalized;
    }

    public static Path entryPath(Path root, String key) {
        String normalized = key.replace('\\', '/');
        if (normalized.endsWith(".yml")) {
            normalized = normalized.substring(0, normalized.length() - 4);
        }
        return resolveInside(root, normalized + ".yml");
    }
}
