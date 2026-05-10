package com.stephanofer.hera.command.paper;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

public interface CommandAccessContext {

    CommandSender sender();

    Entity executor();

    Location location();
}
