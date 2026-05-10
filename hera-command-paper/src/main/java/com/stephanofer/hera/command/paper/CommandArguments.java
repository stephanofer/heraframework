package com.stephanofer.hera.command.paper;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public final class CommandArguments {

    private static final DynamicCommandExceptionType INVALID_ENUM_VALUE = new DynamicCommandExceptionType(value -> SuggestionProviders.toMessage(Component.text("Invalid value: " + value)));

    private CommandArguments() {
    }

    public static CommandArgument<String> word(String name) {
        return CommandArgument.builder(name, StringArgumentType.word(), String.class, (context, argumentName) -> StringArgumentType.getString(context, argumentName))
            .build();
    }

    public static CommandArgument<String> greedyText(String name) {
        return CommandArgument.builder(name, StringArgumentType.greedyString(), String.class, (context, argumentName) -> StringArgumentType.getString(context, argumentName))
            .build();
    }

    public static CommandArgument<Integer> integer(String name) {
        return integer(name, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public static CommandArgument<Integer> integer(String name, int min, int max) {
        return CommandArgument.builder(name, IntegerArgumentType.integer(min, max), Integer.class, (context, argumentName) -> IntegerArgumentType.getInteger(context, argumentName))
            .build();
    }

    public static CommandArgument<Double> doubleArg(String name) {
        return doubleArg(name, -Double.MAX_VALUE, Double.MAX_VALUE);
    }

    public static CommandArgument<Double> doubleArg(String name, double min, double max) {
        return CommandArgument.builder(name, DoubleArgumentType.doubleArg(min, max), Double.class, (context, argumentName) -> DoubleArgumentType.getDouble(context, argumentName))
            .build();
    }

    public static CommandArgument<Boolean> bool(String name) {
        return CommandArgument.builder(name, BoolArgumentType.bool(), Boolean.class, (context, argumentName) -> BoolArgumentType.getBool(context, argumentName))
            .build();
    }

    public static CommandArgument<Player> onlinePlayer(String name) {
        return CommandArgument.builder(name, ArgumentTypes.player(), Player.class, (context, argumentName) -> resolveSinglePlayer(context.getArgument(argumentName, PlayerSelectorArgumentResolver.class), context.getSource()))
            .suggestions(SuggestionProviders.onlinePlayers())
            .build();
    }

    public static <E extends Enum<E>> CommandArgument<E> enumValue(String name, Class<E> enumType) {
        Objects.requireNonNull(enumType, "enumType");
        return CommandArgument.builder(name, StringArgumentType.word(), enumType, (context, argumentName) -> parseEnum(enumType, StringArgumentType.getString(context, argumentName)))
            .suggestions(SuggestionProviders.enums(enumType))
            .build();
    }

    public static <T> CommandArgument<T> of(String name, com.mojang.brigadier.arguments.ArgumentType<?> nativeType, Class<T> valueType, CommandArgumentResolver<T> resolver) {
        return CommandArgument.builder(name, nativeType, valueType, resolver).build();
    }

    private static Player resolveSinglePlayer(PlayerSelectorArgumentResolver resolver, io.papermc.paper.command.brigadier.CommandSourceStack source) throws CommandSyntaxException {
        return resolver.resolve(source).getFirst();
    }

    private static <E extends Enum<E>> E parseEnum(Class<E> enumType, String rawValue) throws CommandSyntaxException {
        return Arrays.stream(enumType.getEnumConstants())
            .filter(constant -> constant.name().equalsIgnoreCase(rawValue))
            .findFirst()
            .orElseThrow(() -> INVALID_ENUM_VALUE.create(rawValue.toLowerCase(Locale.ROOT)));
    }
}
