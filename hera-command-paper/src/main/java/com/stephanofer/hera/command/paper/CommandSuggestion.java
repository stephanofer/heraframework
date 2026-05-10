package com.stephanofer.hera.command.paper;

import java.util.Objects;
import net.kyori.adventure.text.Component;

public record CommandSuggestion(String value, Component tooltip) {

    public CommandSuggestion {
        Objects.requireNonNull(value, "value");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Suggestion value cannot be blank");
        }
    }

    public static CommandSuggestion of(String value) {
        return new CommandSuggestion(value, null);
    }

    public static CommandSuggestion of(String value, Component tooltip) {
        return new CommandSuggestion(value, tooltip);
    }
}
