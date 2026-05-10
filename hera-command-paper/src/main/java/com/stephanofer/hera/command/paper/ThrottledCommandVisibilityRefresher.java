package com.stephanofer.hera.command.paper;

import java.time.Duration;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

final class ThrottledCommandVisibilityRefresher implements CommandVisibilityRefresher {

    private final long cooldownNanos;
    private final ConcurrentMap<UUID, Long> lastRefreshAt = new ConcurrentHashMap<>();

    ThrottledCommandVisibilityRefresher(Duration cooldown) {
        Objects.requireNonNull(cooldown, "cooldown");
        this.cooldownNanos = Math.max(0L, cooldown.toNanos());
    }

    @Override
    public void refresh(Player player) {
        Objects.requireNonNull(player, "player");
        long now = System.nanoTime();
        Long previous = this.lastRefreshAt.putIfAbsent(player.getUniqueId(), now);
        if (previous != null && now - previous < this.cooldownNanos) {
            return;
        }

        this.lastRefreshAt.put(player.getUniqueId(), now);
        player.updateCommands();
    }

    @Override
    public void refresh(Collection<? extends Player> players) {
        Objects.requireNonNull(players, "players");
        for (Player player : players) {
            if (player != null) {
                refresh(player);
            }
        }
    }

    @Override
    public void refreshAllOnline() {
        refresh(Bukkit.getOnlinePlayers());
    }
}
