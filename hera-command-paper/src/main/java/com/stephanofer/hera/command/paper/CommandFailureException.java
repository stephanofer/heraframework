package com.stephanofer.hera.command.paper;

import java.util.Objects;
import net.kyori.adventure.text.Component;

public final class CommandFailureException extends RuntimeException {

    private final Component message;

    public CommandFailureException(Component message) {
        super(message.toString());
        this.message = Objects.requireNonNull(message, "message");
    }

    public Component messageComponent() {
        return this.message;
    }

    public static CommandFailureException of(Component message) {
        return new CommandFailureException(message);
    }
}
