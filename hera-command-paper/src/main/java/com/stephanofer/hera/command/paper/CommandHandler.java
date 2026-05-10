package com.stephanofer.hera.command.paper;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

@FunctionalInterface
public interface CommandHandler {

    int execute(CommandExecutionContext context) throws CommandSyntaxException;
}
