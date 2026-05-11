package com.stephanofer.hera.config;

import java.util.List;
import java.util.Objects;

public record ConfigValidationResult(List<ConfigViolation> violations) {

    private static final ConfigValidationResult VALID = new ConfigValidationResult(List.of());

    public ConfigValidationResult {
        violations = List.copyOf(Objects.requireNonNull(violations, "violations"));
    }

    public static ConfigValidationResult valid() {
        return VALID;
    }

    public static ConfigValidationResult invalid(ConfigViolation... violations) {
        return new ConfigValidationResult(List.of(violations));
    }

    public boolean isValid() {
        return violations.isEmpty();
    }
}
