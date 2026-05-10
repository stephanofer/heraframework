package com.stephanofer.hera.command.paper;

import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.World;

public final class SuggestionProviders {

    private SuggestionProviders() {
    }

    public static CommandSuggestionProvider strings(String... values) {
        Objects.requireNonNull(values, "values");
        return strings(List.of(values));
    }

    public static CommandSuggestionProvider strings(Collection<String> values) {
        Objects.requireNonNull(values, "values");
        List<String> copy = List.copyOf(values);
        return CommandSuggestionProvider.sync(context -> CommandSuggestions.strings(copy, context.remainingLowerCase()));
    }

    public static CommandSuggestionProvider integers(int... values) {
        Objects.requireNonNull(values, "values");
        List<String> copy = Arrays.stream(values).mapToObj(String::valueOf).toList();
        return strings(copy);
    }

    public static <E extends Enum<E>> CommandSuggestionProvider enums(Class<E> enumType) {
        Objects.requireNonNull(enumType, "enumType");
        List<String> values = Arrays.stream(enumType.getEnumConstants())
            .map(constant -> constant.name().toLowerCase(Locale.ROOT))
            .toList();
        return strings(values);
    }

    public static CommandSuggestionProvider snapshotStrings(Supplier<? extends Collection<String>> supplier) {
        Objects.requireNonNull(supplier, "supplier");
        return CommandSuggestionProvider.sync(context -> CommandSuggestions.strings(List.copyOf(supplier.get()), context.remainingLowerCase()));
    }

    public static CommandSuggestionProvider onlinePlayers() {
        return CommandSuggestionProvider.sync(context -> Bukkit.getOnlinePlayers().stream()
            .map(Player::getName)
            .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(context.remainingLowerCase()))
            .map(CommandSuggestion::of)
            .toList());
    }

    public static CommandSuggestionProvider worlds() {
        return CommandSuggestionProvider.sync(context -> Bukkit.getWorlds().stream()
            .map(World::getName)
            .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(context.remainingLowerCase()))
            .map(CommandSuggestion::of)
            .toList());
    }

    public static CommandSuggestionProvider asyncStrings(Function<CommandSuggestionContext, ? extends Collection<String>> supplier) {
        Objects.requireNonNull(supplier, "supplier");
        return context -> CompletableFuture.supplyAsync(() -> CommandSuggestions.strings(List.copyOf(supplier.apply(context)), context.remainingLowerCase()));
    }

    public static CommandSuggestionProvider merge(CommandSuggestionProvider... providers) {
        Objects.requireNonNull(providers, "providers");
        List<CommandSuggestionProvider> copy = List.of(providers);
        return context -> {
            List<CompletableFuture<? extends Iterable<CommandSuggestion>>> futures = new ArrayList<>(copy.size());
            for (CommandSuggestionProvider provider : copy) {
                futures.add(provider.suggest(context));
            }

            return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
            .thenApply(ignored -> {
                List<CommandSuggestion> merged = new ArrayList<>();
                for (CompletableFuture<? extends Iterable<CommandSuggestion>> future : futures) {
                    Iterable<CommandSuggestion> suggestions = future.join();
                    for (CommandSuggestion suggestion : suggestions) {
                        merged.add(suggestion);
                    }
                }
                return List.copyOf(merged);
            });
        };
    }

    static com.mojang.brigadier.Message toMessage(Component component) {
        return MessageComponentSerializer.message().serialize(component);
    }
}
