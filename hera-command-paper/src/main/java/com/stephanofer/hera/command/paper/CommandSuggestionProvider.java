package com.stephanofer.hera.command.paper;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface CommandSuggestionProvider {

    CompletableFuture<? extends Iterable<CommandSuggestion>> suggest(CommandSuggestionContext context);

    static CommandSuggestionProvider sync(Sync sync) {
        Objects.requireNonNull(sync, "sync");
        return context -> CompletableFuture.completedFuture(sync.suggest(context));
    }

    @FunctionalInterface
    interface Sync {
        Iterable<CommandSuggestion> suggest(CommandSuggestionContext context);
    }
}
