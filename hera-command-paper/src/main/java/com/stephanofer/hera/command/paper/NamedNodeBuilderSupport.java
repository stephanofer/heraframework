package com.stephanofer.hera.command.paper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("unchecked")
abstract class NamedNodeBuilderSupport<T extends NamedNodeBuilderSupport<T>> {

    String description;
    String usage;
    String permission;
    boolean restricted;
    SenderPolicy senderPolicy = SenderPolicy.ANY;
    ExecutorPolicy executorPolicy = ExecutorPolicy.ANY;
    CommandRequirement requirement;
    CommandHandler handler;
    final List<CommandNodeSpec> children = new ArrayList<>();
    final List<CommandArgument<?>> linearArguments = new ArrayList<>();

    public T permission(String permission) {
        this.permission = permission;
        return (T) this;
    }

    public T description(String description) {
        this.description = description;
        return (T) this;
    }

    public T usage(String usage) {
        this.usage = usage;
        return (T) this;
    }

    public T restricted(boolean restricted) {
        this.restricted = restricted;
        return (T) this;
    }

    public T senderPolicy(SenderPolicy senderPolicy) {
        this.senderPolicy = senderPolicy;
        return (T) this;
    }

    public T executorPolicy(ExecutorPolicy executorPolicy) {
        this.executorPolicy = executorPolicy;
        return (T) this;
    }

    public T requirement(CommandRequirement requirement) {
        this.requirement = requirement;
        return (T) this;
    }

    public T handler(CommandHandler handler) {
        this.handler = handler;
        return (T) this;
    }

    public T child(CommandNodeSpec child) {
        this.children.add(Objects.requireNonNull(child, "child"));
        return (T) this;
    }

    public T argument(CommandArgument<?> argument) {
        Objects.requireNonNull(argument, "argument");
        for (CommandArgument<?> existing : this.linearArguments) {
            if (existing.name().equals(argument.name())) {
                throw new IllegalArgumentException("Duplicate argument name in the same path: " + argument.name());
            }
        }
        this.linearArguments.add(argument);
        return (T) this;
    }
}
