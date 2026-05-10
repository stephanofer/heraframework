package com.stephanofer.hera.command.paper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class CommandSuggestions {

    private CommandSuggestions() {
    }

    public static List<CommandSuggestion> strings(Collection<String> values, String prefixLowerCase) {
        Objects.requireNonNull(values, "values");
        Objects.requireNonNull(prefixLowerCase, "prefixLowerCase");

        List<CommandSuggestion> suggestions = new ArrayList<>();
        for (String value : values) {
            if (value == null || value.isBlank()) {
                continue;
            }

            if (!prefixLowerCase.isEmpty() && !value.toLowerCase(Locale.ROOT).startsWith(prefixLowerCase)) {
                continue;
            }

            suggestions.add(CommandSuggestion.of(value));
        }

        return List.copyOf(suggestions);
    }
}
