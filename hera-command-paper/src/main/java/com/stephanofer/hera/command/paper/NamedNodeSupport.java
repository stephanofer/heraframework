package com.stephanofer.hera.command.paper;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

final class NamedNodeSupport {

    private NamedNodeSupport() {
    }

    static String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    static String requireLiteral(String value, String label) {
        Objects.requireNonNull(value, label);
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(label + " cannot be blank");
        }

        return trimmed;
    }

    static Set<String> copyAliases(Set<String> aliases) {
        return Set.copyOf(new LinkedHashSet<>(aliases));
    }

    static List<CommandNodeSpec> materializeChildren(List<CommandNodeSpec> directChildren, List<CommandArgument<?>> linearArguments, CommandHandler terminalHandler) {
        List<CommandNodeSpec> children = new ArrayList<>(directChildren);
        if (!linearArguments.isEmpty()) {
            children.add(chainArguments(linearArguments, terminalHandler));
        }

        return List.copyOf(children);
    }

    private static CommandNodeSpec chainArguments(List<CommandArgument<?>> linearArguments, CommandHandler terminalHandler) {
        CommandNodeSpec current = null;
        for (int index = linearArguments.size() - 1; index >= 0; index--) {
            CommandArgument<?> argument = linearArguments.get(index);
            ArgumentNodeSpec.Builder<?> builder = ArgumentNodeSpec.builder(argument);
            if (current != null) {
                builder.child(current);
            }
            if (index == linearArguments.size() - 1) {
                builder.handler(terminalHandler);
            }
            current = builder.build();
        }

        return Objects.requireNonNull(current, "current");
    }

    static void validateNodeShape(String label, CommandHandler handler, List<CommandNodeSpec> children) {
        if (handler == null && children.isEmpty()) {
            throw new IllegalArgumentException(label + " must declare a handler, children, or argument path");
        }
    }
}
