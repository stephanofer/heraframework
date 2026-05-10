package com.stephanofer.hera.command.paper;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

@FunctionalInterface
public interface ExecutorPolicy {

    ExecutorPolicy ANY = executor -> true;
    ExecutorPolicy ENTITY_REQUIRED = executor -> executor != null;
    ExecutorPolicy PLAYER_ONLY = executor -> executor instanceof Player;
    ExecutorPolicy NONE = executor -> executor == null;

    boolean allows(Entity executor);
}
