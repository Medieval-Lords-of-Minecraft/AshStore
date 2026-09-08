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
import me.neoblade298.ashstore.store.StoreMenuConfig;
import me.neoblade298.ashstore.store.StoreMenuEntry;
import me.neoblade298.ashstore.store.StoreManager;
import me.neoblade298.neocore.bukkit.NeoCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

/** Configurable top-level menu containing category links and featured store items. */
public class RootMenu extends StoreMenu {

    private final StoreMenuConfig config;
    private final HashMap<Integer, StoreMenuEntry> slots = new HashMap<>();

    public RootMenu(Player player) {
        this(player, StoreManager.getRootMenu());
    }

    private RootMenu(Player player, StoreMenuConfig config) {
        super(player, Bukkit.createInventory(null, config.size(),
                NeoCore.miniMessage().deserialize(config.title())));
        this.config = config;
        rebuild();
    }

    @Override
    protected void rebuild() {
        inv.clear();
        slots.clear();

        for (StoreMenuEntry entry : config.entries()) {
            if (!isVisible(entry) || entry.slot() < 0 || isControlSlot(entry.slot())) {
                continue;
            }
            StoreMenuEntry current = slots.get(entry.slot());
            if (current == null || entry.priority() < current.priority()) {
                slots.put(entry.slot(), entry);
            }
        }

        int nextSlot = 0;
        for (StoreMenuEntry entry : config.entries()) {
            if (!isVisible(entry) || (entry.slot() >= 0 && !isControlSlot(entry.slot()))) {
                continue;
            }
            while (nextSlot < config.size()
                    && (slots.containsKey(nextSlot) || isControlSlot(nextSlot))) {
                nextSlot++;
            }
            if (nextSlot >= config.size()) {
                break;
            }
            slots.put(nextSlot++, entry);
        }

        for (var entry : slots.entrySet()) {
            inv.setItem(entry.getKey(), renderEntry(entry.getValue()));
        }
        inv.setItem(config.infoSlot(), createInfoIcon());
        inv.setItem(config.balanceSlot(), BalanceDisplay.createIcon(p));
    }

    private boolean isVisible(StoreMenuEntry entry) {
        return !(entry instanceof StoreMenuEntry.Item item) || item.item().isVisibleTo(p);
    }

    private boolean isControlSlot(int slot) {
        return slot == config.infoSlot() || slot == config.balanceSlot();
    }

    private ItemStack renderEntry(StoreMenuEntry entry) {
        if (entry instanceof StoreMenuEntry.Item item) {
            return renderItem(item.item());
        }
        StoreCategory category = ((StoreMenuEntry.Category) entry).category();
        long visibleItems = category.getItems().stream()
                .filter(item -> item.isVisibleTo(p))
                .count();
        return category.getIcon().build(
                NeoCore.miniMessage().deserialize(category.getName()),
                List.of(NeoCore.miniMessage().deserialize(
                        "<gray>" + visibleItems + " item(s)")));
    }

    private ItemStack createInfoIcon() {
        return new StoreIcon(null, Material.BOOK).build(
                NeoCore.miniMessage().deserialize("<gold>Info"),
                List.of(
                        NeoCore.miniMessage().deserialize("<gray>Click here to buy AshCoins."),
                        NeoCore.miniMessage().deserialize(
                                "<gray>All store items are purchasable with AshCoins.")));
    }

    @Override
    public void handleInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (slot == config.infoSlot()) {
            showStoreUrl();
            return;
        }

        StoreMenuEntry entry = slots.get(slot);
        if (entry instanceof StoreMenuEntry.Category category) {
            new ItemMenu(p, category.category()).openInventory();
        } else if (entry instanceof StoreMenuEntry.Item item) {
            selectItem(item.item());
        }
    }

    private void showStoreUrl() {
        String displayUrl = AshStore.inst().getConfig()
                .getString("store-url", "mlmc.tebex.io").trim();
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
    public void handleInventoryDrag(InventoryDragEvent event) {
        event.setCancelled(true);
    }

    @Override
    public void handleInventoryClose(InventoryCloseEvent event) {
    }
}