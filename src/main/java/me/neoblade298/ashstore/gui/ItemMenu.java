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

import me.neoblade298.ashstore.store.StoreCategory;
import me.neoblade298.ashstore.store.StoreItem;
import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/** Menu listing the items of a single category, with paging and purchase handling. */
public class ItemMenu extends StoreMenu {

    private final StoreCategory category;
    private final int page;
    private final HashMap<Integer, StoreItem> slots = new HashMap<>();

    public ItemMenu(Player player, StoreCategory category) {
        this(player, category, 0);
    }

    public ItemMenu(Player player, StoreCategory category, int page) {
        super(player, Bukkit.createInventory(null, category.getSize(),
                NeoCore.miniMessage().deserialize(category.getName())));
        this.category = category;
        this.page = page;
        rebuild();
    }

    @Override
    protected void rebuild() {
        inv.clear();
        slots.clear();

        List<StoreItem> items = getVisibleItems();
        if (category.getItems().stream().anyMatch(StoreItem::hasSlot)) {
            buildSlotted(items);
        } else {
            buildPaged(items);
        }

        inv.setItem(getBackSlot(), CoreInventory.createButton(Material.BARRIER,
                Component.text("Back", NamedTextColor.RED)));
        inv.setItem(getBalanceSlot(), BalanceDisplay.createIcon(p));
    }

    private void buildSlotted(List<StoreItem> items) {
        for (StoreItem item : items) {
            if (!item.hasSlot()) {
                continue;
            }
            StoreItem current = slots.get(item.getSlot());
            if (current == null || item.getPriority() < current.getPriority()) {
                slots.put(item.getSlot(), item);
            }
        }

        int nextSlot = 0;
        for (StoreItem item : items) {
            if (item.hasSlot()) {
                continue;
            }
            while (nextSlot < getPageSize() && slots.containsKey(nextSlot)) {
                nextSlot++;
            }
            if (nextSlot >= getPageSize()) {
                break;
            }
            slots.put(nextSlot++, item);
        }

        for (var entry : slots.entrySet()) {
            inv.setItem(entry.getKey(), renderItem(entry.getValue()));
        }
    }

    private void buildPaged(List<StoreItem> items) {
        int start = page * getPageSize();
        for (int i = 0; i < getPageSize(); i++) {
            int idx = start + i;
            if (idx >= items.size()) {
                break;
            }
            StoreItem item = items.get(idx);
            inv.setItem(i, renderItem(item));
            slots.put(i, item);
        }

        if (page > 0) {
            inv.setItem(getPreviousSlot(), CoreInventory.createButton(Material.ARROW,
                    Component.text("Previous Page", NamedTextColor.YELLOW)));
        }
        if ((page + 1) * getPageSize() < items.size()) {
            inv.setItem(getNextSlot(), CoreInventory.createButton(Material.ARROW,
                    Component.text("Next Page", NamedTextColor.YELLOW)));
        }
    }

    private int getPageSize() {
        return category.getSize() - 9;
    }

    private int getBackSlot() {
        return category.getSize() - 9;
    }

    private int getPreviousSlot() {
        return category.getSize() - 6;
    }

    private int getNextSlot() {
        return category.getSize() - 4;
    }

    private int getBalanceSlot() {
        return category.getSize() - 1;
    }

    private List<StoreItem> getVisibleItems() {
        List<StoreItem> items = new ArrayList<>();
        for (StoreItem item : category.getItems()) {
            if (item.isVisibleTo(p)) {
                items.add(item);
            }
        }
        return items;
    }

    @Override
    public void handleInventoryClick(InventoryClickEvent e) {
        e.setCancelled(true);
        int slot = e.getRawSlot();

        if (slot == getBackSlot()) {
            new RootMenu(p).openInventory();
            return;
        }
        boolean slotted = category.getItems().stream().anyMatch(StoreItem::hasSlot);
        if (!slotted && slot == getPreviousSlot() && page > 0) {
            new ItemMenu(p, category, page - 1).openInventory();
            return;
        }
        if (!slotted && slot == getNextSlot()
            && (page + 1) * getPageSize() < getVisibleItems().size()) {
            new ItemMenu(p, category, page + 1).openInventory();
            return;
        }

        StoreItem item = slots.get(slot);
        if (item != null) {
            selectItem(item);
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
