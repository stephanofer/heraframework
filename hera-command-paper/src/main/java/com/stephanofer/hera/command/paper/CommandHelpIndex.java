package com.stephanofer.hera.command.paper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class CommandHelpIndex {

    private CommandHelpIndex() {
    }

    public static List<CommandHelpEntry> entries(CommandSpec commandSpec) {
        Objects.requireNonNull(commandSpec, "commandSpec");
        List<CommandHelpEntry> entries = new ArrayList<>();
        collect(commandSpec, commandSpec.name(), entries);
        return List.copyOf(entries);
    }

    private static void collect(CommandNodeSpec node, String path, List<CommandHelpEntry> entries) {
        if (node.description() != null || node.usage() != null) {
            entries.add(new CommandHelpEntry(path, node.description(), node.usage(), node.permission()));
        }

        for (CommandNodeSpec child : node.children()) {
            if (child instanceof LiteralNodeSpec literalNode) {
                collect(child, path + " " + literalNode.literal(), entries);
                continue;
            }
            if (child instanceof ArgumentNodeSpec<?> argumentNode) {
                collect(child, path + " <" + argumentNode.argument().name() + ">", entries);
            }
        }
    }
}
