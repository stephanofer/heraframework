package com.stephanofer.hera.command.paper;

import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

@FunctionalInterface
public interface SenderPolicy {

    SenderPolicy ANY = sender -> true;
    SenderPolicy PLAYER_ONLY = sender -> sender instanceof Player;
    SenderPolicy CONSOLE_ONLY = sender -> sender instanceof ConsoleCommandSender;
    SenderPolicy BLOCK_ONLY = sender -> sender instanceof BlockCommandSender;

    boolean allows(CommandSender sender);
}
