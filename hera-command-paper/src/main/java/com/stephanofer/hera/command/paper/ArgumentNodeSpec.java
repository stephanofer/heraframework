package com.stephanofer.hera.command.paper;

import java.util.List;
import java.util.Objects;

public final class ArgumentNodeSpec<T> implements CommandNodeSpec {

    private final CommandArgument<T> argument;
    private final String description;
    private final String usage;
    private final String permission;
    private final boolean restricted;
    private final SenderPolicy senderPolicy;
    private final ExecutorPolicy executorPolicy;
    private final CommandRequirement requirement;
    private final CommandHandler handler;
    private final List<CommandNodeSpec> children;

    private ArgumentNodeSpec(Builder<T> builder) {
        this.argument = Objects.requireNonNull(builder.argument, "argument");
        this.description = NamedNodeSupport.normalize(builder.description);
        this.usage = NamedNodeSupport.normalize(builder.usage);
        this.permission = NamedNodeSupport.normalize(builder.permission);
        this.restricted = builder.restricted;
        this.senderPolicy = Objects.requireNonNull(builder.senderPolicy, "senderPolicy");
        this.executorPolicy = Objects.requireNonNull(builder.executorPolicy, "executorPolicy");
        this.requirement = builder.requirement;
        this.handler = builder.linearArguments.isEmpty() ? builder.handler : null;
        this.children = NamedNodeSupport.materializeChildren(builder.children, builder.linearArguments, builder.handler);
        NamedNodeSupport.validateNodeShape("Argument '" + this.argument.name() + "'", this.handler, this.children);
    }

    public CommandArgument<T> argument() {
        return this.argument;
    }

    @Override
    public String description() {
        return this.description;
    }

    @Override
    public String usage() {
        return this.usage;
    }

    @Override
    public String permission() {
        return this.permission;
    }

    @Override
    public boolean restricted() {
        return this.restricted;
    }

    @Override
    public SenderPolicy senderPolicy() {
        return this.senderPolicy;
    }

    @Override
    public ExecutorPolicy executorPolicy() {
        return this.executorPolicy;
    }

    @Override
    public CommandRequirement requirement() {
        return this.requirement;
    }

    @Override
    public CommandHandler handler() {
        return this.handler;
    }

    @Override
    public List<CommandNodeSpec> children() {
        return this.children;
    }

    public static <T> Builder<T> builder(CommandArgument<T> argument) {
        return new Builder<>(argument);
    }

    public static final class Builder<T> extends NamedNodeBuilderSupport<Builder<T>> {

        private final CommandArgument<T> argument;

        private Builder(CommandArgument<T> argument) {
            this.argument = Objects.requireNonNull(argument, "argument");
        }

        public ArgumentNodeSpec<T> build() {
            return new ArgumentNodeSpec<>(this);
        }
    }
}
