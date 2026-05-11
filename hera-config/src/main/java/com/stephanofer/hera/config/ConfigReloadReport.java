package com.stephanofer.hera.config;

import java.util.List;
import java.util.Objects;

public record ConfigReloadReport(List<ConfigResourceReport> resources) {

    public ConfigReloadReport {
        resources = List.copyOf(Objects.requireNonNull(resources, "resources"));
    }

    public boolean success() {
        return resources.stream().allMatch(ConfigResourceReport::success);
    }

    public long failedCount() {
        return resources.stream().filter(report -> !report.success()).count();
    }

    public long changedCount() {
        return resources.stream().filter(ConfigResourceReport::changed).count();
    }

    public static ConfigReloadReport single(ConfigResourceReport report) {
        return new ConfigReloadReport(List.of(report));
    }
}
