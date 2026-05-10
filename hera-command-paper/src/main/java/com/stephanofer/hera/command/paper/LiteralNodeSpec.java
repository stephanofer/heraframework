package com.stephanofer.hera.command.paper;

import java.util.List;
import java.util.Objects;

public final class LiteralNodeSpec implements CommandNodeSpec {

    private final String literal;
    private final String description;
    private final String usage;
    private final String permission;
    private final boolean restricted;
    private final SenderPolicy senderPolicy;
    private final ExecutorPolicy executorPolicy;
    private final CommandRequirement requirement;
    private final CommandHandler handler;
    private final List<CommandNodeSpec> children;

    private LiteralNodeSpec(Builder builder) {
        this.literal = NamedNodeSupport.requireLiteral(builder.literal, "Literal");
        this.description = NamedNodeSupport.normalize(builder.description);
        this.usage = NamedNodeSupport.normalize(builder.usage);
        this.permission = NamedNodeSupport.normalize(builder.permission);
        this.restricted = builder.restricted;
        this.senderPolicy = Objects.requireNonNull(builder.senderPolicy, "senderPolicy");
        this.executorPolicy = Objects.requireNonNull(builder.executorPolicy, "executorPolicy");
        this.requirement = builder.requirement;
        this.handler = builder.linearArguments.isEmpty() ? builder.handler : null;
        this.children = NamedNodeSupport.materializeChildren(builder.children, builder.linearArguments, builder.handler);
        NamedNodeSupport.validateNodeShape("Literal '" + this.literal + "'", this.handler, this.children);
    }

    public String literal() {
        return this.literal;
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

    public static Builder builder(String literal) {
        return new Builder(literal);
    }

    public static final class Builder extends NamedNodeBuilderSupport<Builder> {

        private final String literal;

        private Builder(String literal) {
            this.literal = NamedNodeSupport.requireLiteral(literal, "Literal");
        }

        public LiteralNodeSpec build() {
            return new LiteralNodeSpec(this);
        }
    }
}
