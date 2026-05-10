package com.stephanofer.hera.command.paper;

public interface CommandSuggestionContext extends CommandExecutionContext {

    String input();

    String remaining();

    String remainingLowerCase();
}
