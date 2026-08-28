package me.neoblade298.ashstore.gui;

import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.ashstore.AshStore;
import me.neoblade298.ashstore.store.StoreCategory;
import me.neoblade298.ashstore.store.StoreIcon;
import me.neoblade298.ashstore.store.StoreManager;
import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

/** Top-level menu listing all store categories in configured slots. */
public class CategoryMenu extends CoreInventory {

    private static final int CATEGORY_SLOTS = 9;
    private static final int INFO_SLOT = 13;
    private final HashMap<Integer, StoreCategory> slots = new HashMap<>();

    public CategoryMenu(Player player) {
        super(player, Bukkit.createInventory(null, 18,
                NeoCore.miniMessage().deserialize("<dark_gray>Store")));
        build();
    }

    private void build() {
        List<StoreCategory> categories = StoreManager.getCategories();

        for (StoreCategory category : categories) {
            if (!category.hasSlot() || category.getSlot() >= CATEGORY_SLOTS) {
                continue;
            }
            StoreCategory current = slots.get(category.getSlot());
            if (current == null || category.getPriority() < current.getPriority()) {
                slots.put(category.getSlot(), category);
            }
        }

        int nextSlot = 0;
        for (StoreCategory category : categories) {
            if (category.hasSlot() && category.getSlot() < CATEGORY_SLOTS) {
                continue;
            }
            while (nextSlot < CATEGORY_SLOTS && slots.containsKey(nextSlot)) {
                nextSlot++;
            }
            if (nextSlot >= CATEGORY_SLOTS) {
                break;
            }
            slots.put(nextSlot++, category);
        }

        for (var entry : slots.entrySet()) {
            StoreCategory category = entry.getValue();
            long visibleItems = category.getItems().stream()
                .filter(item -> item.isVisibleTo(p))
                .count();
            ItemStack icon = category.getIcon().build(
                    NeoCore.miniMessage().deserialize(category.getName()),
                    List.of(NeoCore.miniMessage().deserialize(
                    "<gray>" + visibleItems + " item(s)")));
            inv.setItem(entry.getKey(), icon);
        }

        ItemStack infoIcon = new StoreIcon(null, Material.BOOK).build(
                NeoCore.miniMessage().deserialize("<gold>Info"),
                List.of(NeoCore.miniMessage().deserialize(
                        "<gray>Click here <gray>to buy AshCoins. All store items are purchasable with AshCoins.")));
        inv.setItem(INFO_SLOT, infoIcon);
    }

    @Override
    public void handleInventoryClick(InventoryClickEvent e) {
        e.setCancelled(true);
        if (e.getRawSlot() == INFO_SLOT) {
            showStoreUrl();
            return;
        }
        StoreCategory category = slots.get(e.getRawSlot());
        if (category != null) {
            new ItemMenu(p, category).openInventory();
        }
    }

    private void showStoreUrl() {
        String displayUrl = AshStore.inst().getConfig().getString("store-url", "mlmc.tebex.io").trim();
        if (displayUrl.isEmpty()) {
            displayUrl = "mlmc.tebex.io";
        }
        String openUrl = displayUrl.regionMatches(true, 0, "http://", 0, 7)
                || displayUrl.regionMatches(true, 0, "https://", 0, 8)
                ? displayUrl : "https://" + displayUrl;
        Component link = Component.text(displayUrl, NamedTextColor.YELLOW)
                .decorate(TextDecoration.UNDERLINED)
                .clickEvent(ClickEvent.openUrl(openUrl))
                .hoverEvent(HoverEvent.showText(Component.text("Click to open", NamedTextColor.GREEN)));
        p.closeInventory();
        p.sendMessage(Component.text("Buy AshCoins: ", NamedTextColor.GOLD).append(link));
    }

    @Override
    public void handleInventoryDrag(InventoryDragEvent e) {
        e.setCancelled(true);
    }

    @Override
    public void handleInventoryClose(InventoryCloseEvent e) {
    }
}
