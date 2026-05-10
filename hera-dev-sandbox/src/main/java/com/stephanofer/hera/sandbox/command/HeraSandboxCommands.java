package com.stephanofer.hera.sandbox.command;

import com.stephanofer.hera.command.paper.CommandArguments;
import com.stephanofer.hera.command.paper.CommandArgument;
import com.stephanofer.hera.command.paper.CommandHelpEntry;
import com.stephanofer.hera.command.paper.CommandHelpIndex;
import com.stephanofer.hera.command.paper.CommandResult;
import com.stephanofer.hera.command.paper.CommandSpec;
import com.stephanofer.hera.command.paper.CommandVisibilityRefresher;
import com.stephanofer.hera.command.paper.ExecutorPolicy;
import com.stephanofer.hera.command.paper.LiteralNodeSpec;
import com.stephanofer.hera.command.paper.SenderPolicy;
import com.stephanofer.hera.command.paper.SuggestionProviders;
import java.util.Locale;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

public final class HeraSandboxCommands {

    private HeraSandboxCommands() {
    }

    public static CommandSpec createDemo(CommandVisibilityRefresher visibilityRefresher) {
        CommandSpec[] specRef = new CommandSpec[1];

        CommandSpec spec = CommandSpec.builder("hera-demo")
            .aliases("hdemo")
            .description("Comando demo para probar hera-command-paper")
            .usage("/hera-demo <ping|echo|target|math|mode|playeronly|refresh|help>")
            .handler(context -> {
                sendHelp(context.sender(), specRef[0]);
                return CommandResult.success();
            })
            .child(LiteralNodeSpec.builder("ping")
                .description("Valida ejecución base, sender y executor")
                .usage("/hera-demo ping")
                .handler(context -> {
                    String executorName = context.executor() == null ? "none" : context.executor().getName();
                    context.sender().sendRichMessage("<green>Pong!</green> sender=<yellow>" + context.sender().getName() + "</yellow> executor=<aqua>" + executorName + "</aqua>");
                    return CommandResult.success();
                })
                .build())
            .child(LiteralNodeSpec.builder("echo")
                .description("Prueba argumentos greedy string")
                .usage("/hera-demo echo <message>")
                .argument(CommandArguments.greedyText("message"))
                .handler(context -> {
                    String message = context.requireArgument("message", String.class);
                    context.sender().sendRichMessage("<gray>Echo:</gray> <white>" + message + "</white>");
                    return CommandResult.success();
                })
                .build())
            .child(LiteralNodeSpec.builder("target")
                .description("Prueba argumentos Paper nativos y autocomplete de jugadores")
                .usage("/hera-demo target <player>")
                .argument(CommandArguments.onlinePlayer("player"))
                .handler(context -> {
                    Player target = context.requireArgument("player", Player.class);
                    context.sender().sendRichMessage("<green>Target resuelto:</green> <yellow>" + target.getName() + "</yellow>");
                    return CommandResult.success();
                })
                .build())
            .child(LiteralNodeSpec.builder("math")
                .description("Prueba ramas anidadas y argumentos tipados")
                .child(LiteralNodeSpec.builder("add")
                    .description("Suma dos enteros")
                    .usage("/hera-demo math add <left> <right>")
                    .argument(CommandArguments.integer("left"))
                    .argument(CommandArguments.integer("right"))
                    .handler(context -> {
                        int left = context.requireArgument("left", Integer.class);
                        int right = context.requireArgument("right", Integer.class);
                        context.sender().sendRichMessage("<green>Resultado:</green> <yellow>" + (left + right) + "</yellow>");
                        return CommandResult.success();
                    })
                    .build())
                .child(LiteralNodeSpec.builder("preset")
                    .description("Prueba suggestions estáticos reutilizables")
                    .usage("/hera-demo math preset <amount>")
                    .argument(presetAmountArgument())
                    .handler(context -> {
                        int amount = context.requireArgument("amount", Integer.class);
                        context.sender().sendRichMessage("<green>Preset elegido:</green> <yellow>" + amount + "</yellow>");
                        return CommandResult.success();
                    })
                    .build())
                .build())
            .child(LiteralNodeSpec.builder("mode")
                .description("Prueba enum argument con autocomplete")
                .usage("/hera-demo mode <info|debug|trace>")
                .argument(CommandArguments.enumValue("mode", DemoMode.class))
                .handler(context -> {
                    DemoMode mode = context.requireArgument("mode", DemoMode.class);
                    context.sender().sendRichMessage("<green>Modo seleccionado:</green> <yellow>" + mode.name().toLowerCase(Locale.ROOT) + "</yellow>");
                    return CommandResult.success();
                })
                .build())
            .child(LiteralNodeSpec.builder("playeronly")
                .description("Prueba sender/executor policies")
                .usage("/hera-demo playeronly")
                .senderPolicy(SenderPolicy.PLAYER_ONLY)
                .executorPolicy(ExecutorPolicy.PLAYER_ONLY)
                .handler(context -> {
                    Player player = context.requireExecutor(Player.class);
                    player.sendRichMessage("<green>OK:</green> solo jugadores llegan a esta rama.");
                    return CommandResult.success();
                })
                .build())
            .child(LiteralNodeSpec.builder("refresh")
                .description("Prueba refresh manual de visibilidad para requirements dinámicos")
                .usage("/hera-demo refresh")
                .permission("hera.sandbox.command.refresh")
                .handler(context -> {
                    visibilityRefresher.refreshAllOnline();
                    context.sender().sendRichMessage("<green>Se refrescaron los comandos visibles de los jugadores online.</green>");
                    return CommandResult.success();
                })
                .build())
            .child(LiteralNodeSpec.builder("help")
                .description("Lista metadata de help generada desde el spec")
                .usage("/hera-demo help")
                .handler(context -> {
                    sendHelp(context.sender(), specRef[0]);
                    return CommandResult.success();
                })
                .build())
            .build();

        specRef[0] = spec;
        return spec;
    }

    private static CommandArgument<Integer> presetAmountArgument() {
        return CommandArgument.builder(
                "amount",
                com.mojang.brigadier.arguments.IntegerArgumentType.integer(1, 64),
                Integer.class,
                (brigadierContext, argumentName) -> com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(brigadierContext, argumentName)
            )
            .suggestions(SuggestionProviders.integers(1, 16, 32, 64))
            .build();
    }

    private static void sendHelp(org.bukkit.command.CommandSender sender, CommandSpec spec) {
        List<CommandHelpEntry> entries = CommandHelpIndex.entries(spec);
        sender.sendMessage(Component.text("Hera Sandbox Command Demo", NamedTextColor.GOLD));
        for (CommandHelpEntry entry : entries) {
            String permission = entry.permission() == null ? "sin permiso" : entry.permission();
            sender.sendRichMessage("<gray>-</gray> <yellow>" + entry.path() + "</yellow> <dark_gray>»</dark_gray> <white>" + safe(entry.description()) + "</white> <dark_gray>(" + permission + ")</dark_gray>");
        }
    }

    private static String safe(String value) {
        return value == null ? "sin descripción" : value;
    }
}
