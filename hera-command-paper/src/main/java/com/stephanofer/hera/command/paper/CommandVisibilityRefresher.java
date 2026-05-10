package com.stephanofer.hera.command.paper;

import java.util.Collection;
import org.bukkit.entity.Player;

public interface CommandVisibilityRefresher {

    void refresh(Player player);

    void refresh(Collection<? extends Player> players);

    void refreshAllOnline();
}
