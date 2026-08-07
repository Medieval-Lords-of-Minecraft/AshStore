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

/** Top-level menu listing all store categories in configured slots. */
public class CategoryMenu extends CoreInventory {

    private final HashMap<Integer, StoreCategory> slots = new HashMap<>();

    public CategoryMenu(Player player) {
        super(player, Bukkit.createInventory(null, size(StoreManager.getCategories()),
                NeoCore.miniMessage().deserialize("<dark_gray>Store")));
        build();
    }

    private static int size(List<StoreCategory> categories) {
        int highestSlot = categories.stream()
            .filter(StoreCategory::hasSlot)
            .mapToInt(StoreCategory::getSlot)
            .max()
            .orElse(-1);
        int requiredSlots = Math.max(categories.size(), highestSlot + 1);
        int rows = Math.max(1, (int) Math.ceil(requiredSlots / 9.0));
        rows = Math.min(rows, 6);
        return rows * 9;
    }

    private void build() {
        List<StoreCategory> categories = StoreManager.getCategories();

        for (StoreCategory category : categories) {
            if (!category.hasSlot()) {
                continue;
            }
            StoreCategory current = slots.get(category.getSlot());
            if (current == null || category.getPriority() < current.getPriority()) {
                slots.put(category.getSlot(), category);
            }
        }

        int nextSlot = 0;
        for (StoreCategory category : categories) {
            if (category.hasSlot()) {
                continue;
            }
            while (nextSlot < inv.getSize() && slots.containsKey(nextSlot)) {
                nextSlot++;
            }
            if (nextSlot >= inv.getSize()) {
                break;
            }
            slots.put(nextSlot++, category);
        }

        for (var entry : slots.entrySet()) {
            StoreCategory category = entry.getValue();
            ItemStack icon = category.getIcon().build(
                    NeoCore.miniMessage().deserialize(category.getName()),
                    List.of(NeoCore.miniMessage().deserialize(
                            "<gray>" + category.getItems().size() + " item(s)")));
            inv.setItem(entry.getKey(), icon);
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
