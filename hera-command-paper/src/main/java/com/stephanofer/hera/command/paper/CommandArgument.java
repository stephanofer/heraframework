package com.stephanofer.hera.command.paper;

import com.mojang.brigadier.arguments.ArgumentType;
import java.util.Objects;

public final class CommandArgument<T> {

    private final String name;
    private final ArgumentType<?> nativeType;
    private final Class<T> valueType;
    private final CommandArgumentResolver<T> resolver;
    private final CommandSuggestionProvider suggestions;

    private CommandArgument(Builder<T> builder) {
        this.name = builder.name;
        this.nativeType = builder.nativeType;
        this.valueType = builder.valueType;
        this.resolver = builder.resolver;
        this.suggestions = builder.suggestions;
    }

    public String name() {
        return this.name;
    }

    public ArgumentType<?> nativeType() {
        return this.nativeType;
    }

    public Class<T> valueType() {
        return this.valueType;
    }

    public CommandArgumentResolver<T> resolver() {
        return this.resolver;
    }

    public CommandSuggestionProvider suggestions() {
        return this.suggestions;
    }

    public static <T> Builder<T> builder(String name, ArgumentType<?> nativeType, Class<T> valueType, CommandArgumentResolver<T> resolver) {
        return new Builder<>(name, nativeType, valueType, resolver);
    }

    public static final class Builder<T> {

        private final String name;
        private final ArgumentType<?> nativeType;
        private final Class<T> valueType;
        private final CommandArgumentResolver<T> resolver;
        private CommandSuggestionProvider suggestions;

        private Builder(String name, ArgumentType<?> nativeType, Class<T> valueType, CommandArgumentResolver<T> resolver) {
            this.name = requireName(name);
            this.nativeType = Objects.requireNonNull(nativeType, "nativeType");
            this.valueType = Objects.requireNonNull(valueType, "valueType");
            this.resolver = Objects.requireNonNull(resolver, "resolver");
        }

        public Builder<T> suggestions(CommandSuggestionProvider suggestions) {
            this.suggestions = suggestions;
            return this;
        }

        public CommandArgument<T> build() {
            return new CommandArgument<>(this);
        }
    }

    static String requireName(String name) {
        Objects.requireNonNull(name, "name");
        if (name.isBlank()) {
            throw new IllegalArgumentException("Argument name cannot be blank");
        }

        return name;
    }
}
