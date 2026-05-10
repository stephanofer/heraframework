package com.stephanofer.hera.command.paper;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

final class BrigadierCommandContext implements CommandExecutionContext, CommandSuggestionContext {

    private final CommandSourceStack source;
    private final Map<String, Object> arguments;
    private final String input;
    private final String remaining;
    private final String remainingLowerCase;

    BrigadierCommandContext(CommandSourceStack source, Map<String, Object> arguments, String input, String remaining, String remainingLowerCase) {
        this.source = Objects.requireNonNull(source, "source");
        this.arguments = Objects.requireNonNull(arguments, "arguments");
        this.input = input;
        this.remaining = remaining;
        this.remainingLowerCase = remainingLowerCase;
    }

    @Override
    public CommandSender sender() {
        return this.source.getSender();
    }

    @Override
    public Entity executor() {
        return this.source.getExecutor();
    }

    @Override
    public Location location() {
        return this.source.getLocation();
    }

    @Override
    public <T> Optional<T> argument(String name, Class<T> type) {
        Object value = this.arguments.get(name);
        if (value == null || !type.isInstance(value)) {
            return Optional.empty();
        }

        return Optional.of(type.cast(value));
    }

    @Override
    public String input() {
        return this.input == null ? "" : this.input;
    }

    @Override
    public String remaining() {
        return this.remaining == null ? "" : this.remaining;
    }

    @Override
    public String remainingLowerCase() {
        return this.remainingLowerCase == null ? "" : this.remainingLowerCase;
    }
}
