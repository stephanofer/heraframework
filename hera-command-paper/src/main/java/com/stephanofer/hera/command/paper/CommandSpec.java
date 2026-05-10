package com.stephanofer.hera.command.paper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class CommandSpec implements CommandNodeSpec {

    private final String name;
    private final Set<String> aliases;
    private final String description;
    private final String usage;
    private final String permission;
    private final boolean restricted;
    private final SenderPolicy senderPolicy;
    private final ExecutorPolicy executorPolicy;
    private final CommandRequirement requirement;
    private final CommandHandler handler;
    private final List<CommandNodeSpec> children;

    private CommandSpec(Builder builder) {
        this.name = NamedNodeSupport.requireLiteral(builder.name, "Command name");
        this.aliases = NamedNodeSupport.copyAliases(builder.aliases);
        this.description = NamedNodeSupport.normalize(builder.description);
        this.usage = NamedNodeSupport.normalize(builder.usage);
        this.permission = NamedNodeSupport.normalize(builder.permission);
        this.restricted = builder.restricted;
        this.senderPolicy = Objects.requireNonNull(builder.senderPolicy, "senderPolicy");
        this.executorPolicy = Objects.requireNonNull(builder.executorPolicy, "executorPolicy");
        this.requirement = builder.requirement;
        this.handler = builder.linearArguments.isEmpty() ? builder.handler : null;
        this.children = NamedNodeSupport.materializeChildren(builder.children, builder.linearArguments, builder.handler);
        NamedNodeSupport.validateNodeShape("Command '" + this.name + "'", this.handler, this.children);
    }

    public String name() {
        return this.name;
    }

    public Set<String> aliases() {
        return this.aliases;
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

    public static Builder builder(String name) {
        return new Builder(name);
    }

    public static final class Builder extends NamedNodeBuilderSupport<Builder> {

        private final String name;
        private final Set<String> aliases = new LinkedHashSet<>();

        private Builder(String name) {
            this.name = NamedNodeSupport.requireLiteral(name, "Command name");
        }

        public Builder aliases(String... aliases) {
            Objects.requireNonNull(aliases, "aliases");
            for (String alias : aliases) {
                this.aliases.add(NamedNodeSupport.requireLiteral(alias, "Alias"));
            }
            return this;
        }

        public Builder aliases(Collection<String> aliases) {
            Objects.requireNonNull(aliases, "aliases");
            for (String alias : aliases) {
                this.aliases.add(NamedNodeSupport.requireLiteral(alias, "Alias"));
            }
            return this;
        }

        public CommandSpec build() {
            return new CommandSpec(this);
        }
    }
}
