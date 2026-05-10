package com.stephanofer.hera.command.paper;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

final class PaperCommandCompiler {

    LiteralCommandNode<CommandSourceStack> compile(CommandSpec spec) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(spec.name());
        applyCommon(root, spec, List.of());
        for (CommandNodeSpec child : spec.children()) {
            root.then(compileNode(child, List.of()));
        }
        return root.build();
    }

    private ArgumentBuilder<CommandSourceStack, ?> compileNode(CommandNodeSpec node, List<CommandArgument<?>> pathArguments) {
        if (node instanceof LiteralNodeSpec literalNode) {
            LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal(literalNode.literal());
            applyCommon(builder, literalNode, pathArguments);
            for (CommandNodeSpec child : literalNode.children()) {
                builder.then(compileNode(child, pathArguments));
            }
            return builder;
        }

        if (node instanceof ArgumentNodeSpec<?> argumentNode) {
            return compileArgumentNode(argumentNode, pathArguments);
        }

        throw new IllegalArgumentException("Unsupported command node type: " + node.getClass().getName());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private ArgumentBuilder<CommandSourceStack, ?> compileArgumentNode(ArgumentNodeSpec<?> argumentNode, List<CommandArgument<?>> pathArguments) {
        CommandArgument<?> argument = argumentNode.argument();
        RequiredArgumentBuilder<CommandSourceStack, ?> builder = Commands.argument(argument.name(), (ArgumentType) argument.nativeType());
        List<CommandArgument<?>> nextPathArguments = append(pathArguments, argument);
        applyCommon(builder, argumentNode, nextPathArguments);
        if (argument.suggestions() != null) {
            builder.suggests((context, suggestionsBuilder) -> provideSuggestions(argument.suggestions(), context, suggestionsBuilder, pathArguments));
        }
        for (CommandNodeSpec child : argumentNode.children()) {
            builder.then(compileNode(child, nextPathArguments));
        }
        return builder;
    }

    private void applyCommon(ArgumentBuilder<CommandSourceStack, ?> builder, CommandNodeSpec node, List<CommandArgument<?>> resolvedArguments) {
        builder.requires(requirementFor(node));
        if (node.handler() != null) {
            builder.executes(context -> execute(node.handler(), context, resolvedArguments));
        }
    }

    private Predicate<CommandSourceStack> requirementFor(CommandNodeSpec node) {
        Predicate<CommandSourceStack> predicate = source -> {
            CommandAccessContext context = new BrigadierCommandContext(source, Map.of(), null, null, null);
            if (node.permission() != null && !context.sender().hasPermission(node.permission())) {
                return false;
            }
            if (!node.senderPolicy().allows(context.sender())) {
                return false;
            }
            if (!node.executorPolicy().allows(context.executor())) {
                return false;
            }
            return node.requirement() == null || node.requirement().test(context);
        };

        return node.restricted() ? Commands.restricted(predicate) : predicate;
    }

    private int execute(CommandHandler handler, CommandContext<CommandSourceStack> context, List<CommandArgument<?>> resolvedArguments) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        try {
            return handler.execute(new BrigadierCommandContext(
                context.getSource(),
                resolveArguments(context, resolvedArguments),
                null,
                null,
                null
            ));
        } catch (CommandFailureException exception) {
            context.getSource().getSender().sendMessage(exception.messageComponent());
            return Command.SINGLE_SUCCESS;
        }
    }

    private CompletableFuture<Suggestions> provideSuggestions(
        CommandSuggestionProvider provider,
        CommandContext<CommandSourceStack> context,
        SuggestionsBuilder builder,
        List<CommandArgument<?>> resolvedArguments
    ) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        BrigadierCommandContext suggestionContext = new BrigadierCommandContext(
            context.getSource(),
            resolveArguments(context, resolvedArguments),
            builder.getInput(),
            builder.getRemaining(),
            builder.getRemainingLowerCase()
        );

        return provider.suggest(suggestionContext).thenApply(suggestions -> {
            for (CommandSuggestion suggestion : suggestions) {
                if (suggestion.tooltip() == null) {
                    builder.suggest(suggestion.value());
                    continue;
                }

                builder.suggest(suggestion.value(), SuggestionProviders.toMessage(suggestion.tooltip()));
            }
            return builder.build();
        });
    }

    private Map<String, Object> resolveArguments(CommandContext<CommandSourceStack> context, List<CommandArgument<?>> argumentSpecs) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        Map<String, Object> values = new LinkedHashMap<>();
        for (CommandArgument<?> argument : argumentSpecs) {
            values.put(argument.name(), argument.resolver().resolve(context, argument.name()));
        }
        return Map.copyOf(values);
    }

    private List<CommandArgument<?>> append(List<CommandArgument<?>> arguments, CommandArgument<?> next) {
        List<CommandArgument<?>> copy = new ArrayList<>(arguments);
        copy.add(next);
        return List.copyOf(copy);
    }
}
