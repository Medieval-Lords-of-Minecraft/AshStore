package me.neoblade298.ashstore.gui;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

import me.neoblade298.ashstore.store.StoreItem;
import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/** Standard confirm/cancel dialog displayed before any store purchase. */
public class PurchaseConfirmationMenu extends CoreInventory {

    private static final int SLOT_CONFIRM = 11;
    private static final int SLOT_ITEM = 13;
    private static final int SLOT_CANCEL = 15;

    private final Runnable confirmAction;
    private final Runnable cancelAction;
    private boolean handled;

    public PurchaseConfirmationMenu(Player player, StoreItem item,
                                    Runnable confirmAction, Runnable cancelAction) {
        super(player, Bukkit.createInventory(null, 27,
                NeoCore.miniMessage().deserialize("<dark_gray>Confirm Purchase")));
        this.confirmAction = confirmAction;
        this.cancelAction = cancelAction;

        inv.setItem(SLOT_CONFIRM, CoreInventory.createButton(Material.LIME_CONCRETE,
                Component.text("Confirm Purchase", NamedTextColor.GREEN)));
        inv.setItem(SLOT_ITEM, item.getIcon().build(
                NeoCore.miniMessage().deserialize(item.getName()),
                List.of(
                    NeoCore.miniMessage().deserialize(
                        "<gold>Price: <yellow>" + item.getPrice() + "</yellow> AshCoins"),
                    NeoCore.miniMessage().deserialize("<gray>Your balance will be charged immediately."))));
        inv.setItem(SLOT_CANCEL, CoreInventory.createButton(Material.RED_CONCRETE,
                Component.text("Cancel", NamedTextColor.RED)));
    }

    @Override
    public void handleInventoryClick(InventoryClickEvent e) {
        e.setCancelled(true);
        if (handled) {
            return;
        }
        if (e.getRawSlot() == SLOT_CONFIRM) {
            handled = true;
            confirmAction.run();
        } else if (e.getRawSlot() == SLOT_CANCEL) {
            handled = true;
            cancelAction.run();
        }
    }

    @Override
    public void handleInventoryDrag(InventoryDragEvent e) {
        e.setCancelled(true);
    }

    @Override
    public void handleInventoryClose(InventoryCloseEvent e) {
    }
}