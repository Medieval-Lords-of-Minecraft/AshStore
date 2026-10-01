package me.neoblade298.ashstore.gui;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.ashstore.AshStore;
import me.neoblade298.ashstore.player.PlayerData;
import me.neoblade298.ashstore.player.PlayerManager;
import me.neoblade298.ashstore.store.StoreItem;
import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neocore.bukkit.util.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

/** Shared rendering and interaction behavior for menus containing store items. */
public abstract class StoreMenu extends CoreInventory {

    protected StoreMenu(Player player, Inventory inventory) {
        super(player, inventory);
    }

    protected ItemStack renderItem(StoreItem item) {
        List<Component> lore = new ArrayList<>();
        for (String line : item.getLore()) {
            lore.add(NeoCore.miniMessage().deserialize(line));
        }
        if (item.isOwnedBy(p)) {
            lore.add(Component.empty());
            lore.add(NeoCore.miniMessage().deserialize("<green>Owned"));
            if (item.hasDetails()) {
                lore.add(NeoCore.miniMessage().deserialize("<green>Click to view details"));
            }
        } else if (item.isPurchasable()) {
            var price = PriceDisplay.getDetails(item.getPrice());
            lore.add(Component.empty());
            lore.addAll(PriceDisplay.lore(price));
            if (item.hasPermission() && !p.hasPermission(item.getPermission())) {
                lore.add(NeoCore.miniMessage().deserialize("<red>You don't have access to this item"));
            } else if (item.hasDetails()) {
                lore.add(NeoCore.miniMessage().deserialize("<green>Click to view details"));
            } else {
                lore.add(NeoCore.miniMessage().deserialize("<green>Click to purchase"));
            }
        } else if (item.hasDetails()) {
            lore.add(Component.empty());
            lore.add(NeoCore.miniMessage().deserialize("<green>Click to view details"));
        }
        return item.getIcon().build(NeoCore.miniMessage().deserialize(item.getName()), lore);
    }

    protected void selectItem(StoreItem item) {
        if (item.isPurchasable() && !item.isOwnedBy(p)) {
            if (item.hasDetails()) {
                new ItemDetailsMenu(p, item, this).openInventory();
            } else {
                PurchaseConfirmationDialog.show(p, item, () -> purchase(item));
            }
        } else if (item.hasDetails()) {
            new ItemDetailsMenu(p, item, this).openInventory();
        }
    }

    void purchase(StoreItem item) {
        if (item.isOwnedBy(p)) {
            Util.msgRaw(p, "<green>You already own this item.");
            rebuild();
            openInventory();
            return;
        }

        if (item.hasNegatePermission() && p.hasPermission(item.getNegatePermission())) {
            rebuild();
            openInventory();
            return;
        }

        if (item.hasPermission() && !p.hasPermission(item.getPermission())) {
            Util.msgRaw(p, "<red>You don't have access to purchase this item.");
            openInventory();
            return;
        }

        PlayerData data = PlayerManager.get(p);
        if (data == null) {
            Util.msgRaw(p, "<red>Your data hasn't loaded yet. Try again shortly.");
            openInventory();
            return;
        }

        long price = AshStore.inst().getSaleManager().getPrice(item.getPrice());
        if (!data.canAfford(price)) {
            Util.msgRaw(p, "<red>You need <yellow>" + price
                    + "</yellow> AshCoins but only have <yellow>" + data.getCoins() + "</yellow>.");
            openInventory();
            return;
        }

        data.deduct(price);
        for (String command : item.getCommands()) {
            String parsed = command
                    .replace("%player%", p.getName())
                    .replace("%uuid%", p.getUniqueId().toString());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsed);
        }

        AshStore.inst().getLogger().info(p.getName() + " (" + p.getUniqueId()
                + ") purchased " + item.getName() + " for " + price + " AshCoins.");

        String broadcast = AshStore.inst().getConfig().getString("messages.broadcast",
            "<yellow><player></yellow> just purchased <item> from <yellow>/store</yellow>!");
        if (broadcast != null && !broadcast.isBlank()) {
            Bukkit.broadcast(NeoCore.miniMessage().deserialize(broadcast,
                Placeholder.unparsed("player", p.getName()),
                Placeholder.parsed("item", item.getName())));
        }
        rebuild();
        openInventory();
    }

    protected abstract void rebuild();
}