package me.neoblade298.ashstore.gui;

import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.ashstore.store.StoreCategory;
import me.neoblade298.ashstore.store.StoreManager;
import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.bukkit.inventories.CoreInventory;

/** Top-level menu listing all store categories, sorted alphabetically. */
public class CategoryMenu extends CoreInventory {

    private final HashMap<Integer, StoreCategory> slots = new HashMap<>();

    public CategoryMenu(Player player) {
        super(player, Bukkit.createInventory(null, size(StoreManager.getCategories().size()),
                NeoCore.miniMessage().deserialize("<dark_gray>Store")));
        build();
    }

    private static int size(int count) {
        int rows = Math.max(1, (int) Math.ceil(count / 9.0));
        rows = Math.min(rows, 6);
        return rows * 9;
    }

    private void build() {
        List<StoreCategory> categories = StoreManager.getCategories();
        int slot = 0;
        for (StoreCategory category : categories) {
            if (slot >= inv.getSize()) {
                break;
            }
            ItemStack icon = category.getIcon().build(
                    NeoCore.miniMessage().deserialize(category.getName()),
                    List.of(NeoCore.miniMessage().deserialize(
                            "<gray>" + category.getItems().size() + " item(s)")));
            inv.setItem(slot, icon);
            slots.put(slot, category);
            slot++;
        }
    }

    @Override
    public void handleInventoryClick(InventoryClickEvent e) {
        e.setCancelled(true);
        StoreCategory category = slots.get(e.getRawSlot());
        if (category != null) {
            new ItemMenu(p, category).openInventory();
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
