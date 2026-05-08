package fr.maxlego08.zauctionhouse.buttons.shulker;

import fr.maxlego08.menu.api.button.Button;
import fr.maxlego08.menu.api.engine.InventoryEngine;
import fr.maxlego08.menu.api.utils.Placeholders;
import fr.maxlego08.zauctionhouse.api.AuctionPlugin;
import fr.maxlego08.zauctionhouse.api.cache.PlayerCacheKey;
import fr.maxlego08.zauctionhouse.api.item.Item;
import fr.maxlego08.zauctionhouse.api.item.items.AuctionItem;
import fr.maxlego08.zauctionhouse.utils.ShulkerHelper;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class ShulkerNavigationButton extends Button {

    private final AuctionPlugin plugin;
    private final boolean isNext;

    public ShulkerNavigationButton(Plugin plugin, boolean isNext) {
        this.plugin = (AuctionPlugin) plugin;
        this.isNext = isNext;
    }

    @Override
    public boolean hasPermission() {
        return true;
    }

    @Override
    public boolean checkPermission(@NonNull Player player, @NonNull InventoryEngine inventory, @NonNull Placeholders placeholders) {
        var cache = this.plugin.getAuctionManager().getCache(player);
        Item item = cache.get(PlayerCacheKey.ITEM_SHOW);

        if (!(item instanceof AuctionItem auctionItem)) {
            return false;
        }

        List<ItemStack> shulkers = ShulkerHelper.getShulkerBoxes(auctionItem.getItemStacks());
        if (shulkers.size() <= 1) {
            return false;
        }

        int currentIndex = cache.get(PlayerCacheKey.SHULKER_INDEX, 0);

        if (isNext) {
            return currentIndex < shulkers.size() - 1;
        } else {
            return currentIndex > 0;
        }
    }

    @Override
    public void onClick(@NotNull Player player, @NotNull InventoryClickEvent event, @NotNull InventoryEngine inventory, int slot, @NotNull Placeholders placeholders) {
        super.onClick(player, event, inventory, slot, placeholders);

        var cache = this.plugin.getAuctionManager().getCache(player);
        Item item = cache.get(PlayerCacheKey.ITEM_SHOW);

        if (!(item instanceof AuctionItem auctionItem)) {
            return;
        }

        List<ItemStack> shulkers = ShulkerHelper.getShulkerBoxes(auctionItem.getItemStacks());
        if (shulkers.isEmpty()) {
            return;
        }

        int currentIndex = cache.get(PlayerCacheKey.SHULKER_INDEX, 0);

        if (isNext) {
            currentIndex = Math.min(currentIndex + 1, shulkers.size() - 1);
        } else {
            currentIndex = Math.max(currentIndex - 1, 0);
        }

        cache.set(PlayerCacheKey.SHULKER_INDEX, currentIndex);

        List<ItemStack> shulkerContent = ShulkerHelper.getShulkerContent(shulkers.get(currentIndex));
        cache.set(PlayerCacheKey.SHULKER_ITEMS, shulkerContent);

        this.plugin.getInventoriesLoader().getInventoryManager().updateInventory(player);
    }
}
