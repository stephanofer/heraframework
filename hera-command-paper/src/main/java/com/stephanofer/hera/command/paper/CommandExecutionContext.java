package com.stephanofer.hera.command.paper;

import java.util.Optional;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

public interface CommandExecutionContext extends CommandAccessContext {

    @Override
    CommandSender sender();

    @Override
    Entity executor();

    @Override
    Location location();

    <T> Optional<T> argument(String name, Class<T> type);

    default <T> T requireArgument(String name, Class<T> type) {
        return argument(name, type)
            .orElseThrow(() -> new IllegalArgumentException("Missing argument '" + name + "' as " + type.getSimpleName()));
    }

    default <T extends Entity> Optional<T> executor(Class<T> type) {
        Entity executor = executor();
        if (executor == null || !type.isInstance(executor)) {
            return Optional.empty();
        }

        return Optional.of(type.cast(executor));
    }

    default <T extends Entity> T requireExecutor(Class<T> type) {
        return executor(type)
            .orElseThrow(() -> new IllegalArgumentException("Command executor is not a " + type.getSimpleName()));
    }
}
