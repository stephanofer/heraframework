package com.stephanofer.hera.command.paper;

@FunctionalInterface
public interface CommandRequirement {

    boolean test(CommandAccessContext context);
}
