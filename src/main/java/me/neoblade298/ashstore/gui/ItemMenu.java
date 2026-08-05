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
import org.bukkit.inventory.ItemStack;

import me.neoblade298.ashstore.player.PlayerData;
import me.neoblade298.ashstore.player.PlayerManager;
import me.neoblade298.ashstore.store.StoreCategory;
import me.neoblade298.ashstore.store.StoreItem;
import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neocore.bukkit.util.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/** Menu listing the items of a single category, with paging and purchase handling. */
public class ItemMenu extends CoreInventory {

    private static final int PAGE_SIZE = 45;
    private static final int SLOT_BACK = 45;
    private static final int SLOT_PREV = 48;
    private static final int SLOT_NEXT = 50;

    private final StoreCategory category;
    private final int page;
    private final HashMap<Integer, StoreItem> slots = new HashMap<>();

    public ItemMenu(Player player, StoreCategory category) {
        this(player, category, 0);
    }

    public ItemMenu(Player player, StoreCategory category, int page) {
        super(player, Bukkit.createInventory(null, 54,
                NeoCore.miniMessage().deserialize(category.getName())));
        this.category = category;
        this.page = page;
        build();
    }

    private void build() {
        inv.clear();
        slots.clear();

        List<StoreItem> items = getVisibleItems();
        if (category.getItems().stream().anyMatch(StoreItem::hasSlot)) {
            buildSlotted(items);
        } else {
            buildPaged(items);
        }

        inv.setItem(SLOT_BACK, CoreInventory.createButton(Material.BARRIER,
                Component.text("Back", NamedTextColor.RED)));
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
            while (nextSlot < PAGE_SIZE && slots.containsKey(nextSlot)) {
                nextSlot++;
            }
            if (nextSlot >= PAGE_SIZE) {
                break;
            }
            slots.put(nextSlot++, item);
        }

        for (var entry : slots.entrySet()) {
            inv.setItem(entry.getKey(), renderItem(entry.getValue()));
        }
    }

    private void buildPaged(List<StoreItem> items) {
        int start = page * PAGE_SIZE;
        for (int i = 0; i < PAGE_SIZE; i++) {
            int idx = start + i;
            if (idx >= items.size()) {
                break;
            }
            StoreItem item = items.get(idx);
            inv.setItem(i, renderItem(item));
            slots.put(i, item);
        }

        if (page > 0) {
            inv.setItem(SLOT_PREV, CoreInventory.createButton(Material.ARROW,
                    Component.text("Previous Page", NamedTextColor.YELLOW)));
        }
        if ((page + 1) * PAGE_SIZE < items.size()) {
            inv.setItem(SLOT_NEXT, CoreInventory.createButton(Material.ARROW,
                    Component.text("Next Page", NamedTextColor.YELLOW)));
        }
    }

    private List<StoreItem> getVisibleItems() {
        List<StoreItem> items = new ArrayList<>();
        for (StoreItem item : category.getItems()) {
            if ((!item.hasViewPermission() || p.hasPermission(item.getViewPermission()))
                    && (!item.hasNegatePermission() || !p.hasPermission(item.getNegatePermission()))) {
                items.add(item);
            }
        }
        return items;
    }

    private ItemStack renderItem(StoreItem item) {
        List<Component> lore = new ArrayList<>();
        for (String line : item.getLore()) {
            lore.add(NeoCore.miniMessage().deserialize(line));
        }
        if (item.isPurchasable()) {
            lore.add(Component.empty());
            lore.add(NeoCore.miniMessage().deserialize("<gold>Price: <yellow>" + item.getPrice() + "</yellow> AshCoins"));
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

    @Override
    public void handleInventoryClick(InventoryClickEvent e) {
        e.setCancelled(true);
        int slot = e.getRawSlot();

        if (slot == SLOT_BACK) {
            new CategoryMenu(p).openInventory();
            return;
        }
        boolean slotted = category.getItems().stream().anyMatch(StoreItem::hasSlot);
        if (!slotted && slot == SLOT_PREV && page > 0) {
            new ItemMenu(p, category, page - 1).openInventory();
            return;
        }
        if (!slotted && slot == SLOT_NEXT && (page + 1) * PAGE_SIZE < getVisibleItems().size()) {
            new ItemMenu(p, category, page + 1).openInventory();
            return;
        }

        StoreItem item = slots.get(slot);
        if (item != null && item.isPurchasable()) {
            if (item.hasDetails()) {
                new ItemDetailsMenu(p, item, this).openInventory();
            } else {
                PurchaseConfirmationDialog.show(p, item, () -> purchase(item));
            }
        } else if (item != null && item.hasDetails()) {
            new ItemDetailsMenu(p, item, this).openInventory();
        }
    }

    void purchase(StoreItem item) {
        if (item.hasNegatePermission() && p.hasPermission(item.getNegatePermission())) {
            build();
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

        if (!data.canAfford(item.getPrice())) {
            Util.msgRaw(p, "<red>You need <yellow>" + item.getPrice()
                    + "</yellow> AshCoins but only have <yellow>" + data.getCoins() + "</yellow>.");
            openInventory();
            return;
        }

        data.deduct(item.getPrice());
        for (String cmd : item.getCommands()) {
            String parsed = cmd
                    .replace("%player%", p.getName())
                    .replace("%uuid%", p.getUniqueId().toString());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsed);
        }

        Util.msgRaw(p, "<green>Purchase successful! You now have <yellow>"
                + data.getCoins() + "</yellow> AshCoins.");
        build();
        openInventory();
    }

    @Override
    public void handleInventoryDrag(InventoryDragEvent e) {
        e.setCancelled(true);
    }

    @Override
    public void handleInventoryClose(InventoryCloseEvent e) {
    }
}
