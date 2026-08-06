package me.neoblade298.ashstore.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

import me.neoblade298.ashstore.AshStore;
import me.neoblade298.ashstore.store.StoreItem;
import me.neoblade298.ashstore.store.StoreItemDetailGroup;
import me.neoblade298.ashstore.store.StoreItemDetails;
import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/** Expanded perk groups and purchase controls for a store item. */
public class ItemDetailsMenu extends CoreInventory {

    private static final int CONTENT_SIZE = 45;

    private final StoreItem item;
    private final StoreItemDetails details;
    private final ItemMenu parent;

    public ItemDetailsMenu(Player player, StoreItem item, ItemMenu parent) {
        super(player, Bukkit.createInventory(null, 54,
                NeoCore.miniMessage().deserialize(item.getDetails().getTitle())));
        this.item = item;
        this.details = item.getDetails();
        this.parent = parent;
        build();
    }

    private void build() {
        HashMap<Integer, StoreItemDetailGroup> groups = new HashMap<>();
        for (StoreItemDetailGroup group : details.getGroups()) {
            if (group.hasSlot()) {
                groups.put(group.getSlot(), group);
            }
        }

        int nextSlot = 0;
        for (StoreItemDetailGroup group : details.getGroups()) {
            if (group.hasSlot()) {
                continue;
            }
            while (nextSlot < CONTENT_SIZE && groups.containsKey(nextSlot)) {
                nextSlot++;
            }
            if (nextSlot >= CONTENT_SIZE) {
                break;
            }
            groups.put(nextSlot++, group);
        }

        for (var entry : groups.entrySet()) {
            StoreItemDetailGroup group = entry.getValue();
            List<Component> lore = group.getLore().stream()
                    .map(line -> NeoCore.miniMessage().deserialize(line))
                    .toList();
            inv.setItem(entry.getKey(), group.getIcon().build(
                    NeoCore.miniMessage().deserialize(group.getName()), lore));
        }

        inv.setItem(details.getBackSlot(), CoreInventory.createButton(Material.BARRIER,
                Component.text("Back", NamedTextColor.RED)));
        if (item.isPurchasable()) {
            inv.setItem(details.getPurchaseSlot(), renderPurchaseButton());
        }
    }

    private org.bukkit.inventory.ItemStack renderPurchaseButton() {
        List<Component> lore = new ArrayList<>();
        long price = AshStore.inst().getSaleManager().getPrice(item.getPrice());
        lore.add(NeoCore.miniMessage().deserialize(
            "<gold>Price: <yellow>" + price + "</yellow> AshCoins"));
        if (item.hasPermission() && !p.hasPermission(item.getPermission())) {
            lore.add(NeoCore.miniMessage().deserialize("<red>You don't have access to this item"));
        } else {
            lore.add(NeoCore.miniMessage().deserialize("<green>Click to purchase"));
        }
        return details.getPurchaseIcon().build(
                NeoCore.miniMessage().deserialize(details.getPurchaseName()), lore);
    }

    @Override
    public void handleInventoryClick(InventoryClickEvent e) {
        e.setCancelled(true);
        int slot = e.getRawSlot();
        if (slot == details.getBackSlot()) {
            parent.openInventory();
        } else if (item.isPurchasable() && slot == details.getPurchaseSlot()) {
            PurchaseConfirmationDialog.show(p, item, () -> parent.purchase(item));
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