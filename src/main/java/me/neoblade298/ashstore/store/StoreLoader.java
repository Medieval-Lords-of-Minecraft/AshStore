package me.neoblade298.ashstore.store;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;

import me.neoblade298.ashstore.AshStore;
import me.neoblade298.neocore.bukkit.io.FileLoader;
import me.neoblade298.neocore.shared.io.Config;
import me.neoblade298.neocore.shared.io.Section;

/** Loads one {@link StoreCategory} (with its items) from each YAML file in the categories/ folder. */
public class StoreLoader implements FileLoader {

    @Override
    public void load(Config cfg, File f) {
        String id = f.getName().replaceAll("\\.ya?ml$", "");
        try {
            String name = cfg.getString("name", id);
            int slot = cfg.getInt("slot", -1);
            int priority = cfg.getInt("priority", 10);
            StoreIcon icon = StoreIcon.from(cfg.contains("icon") ? cfg.getSection("icon") : null);

            if (cfg.contains("slot") && (slot < 0 || slot >= 54)) {
                AshStore.inst().getLogger().warning(
                    "Store category '" + id + "' has invalid slot " + slot + "; expected 0-53.");
                slot = -1;
            }

            StoreCategory category = new StoreCategory(id, name, slot, priority, icon);

            if (cfg.contains("items")) {
                Section items = cfg.getSection("items");
                for (String key : items.getKeys()) {
                    Section is = items.getSection(key);
                    if (is == null) {
                        continue;
                    }
                    StoreItem item = loadItem(key, is);
                    if (item != null) {
                        category.addItem(item);
                    }
                }
                category.sortItems();
            }

            StoreManager.register(category);
        } catch (Exception e) {
            AshStore.inst().getLogger().warning(
                "Failed to load store category '" + id + "': " + e.getMessage());
        }
    }

    private StoreItem loadItem(String key, Section is) {
        String name = is.getString("name", key);
        long price = is.getInt("price", 0);
        int slot = is.getInt("slot", -1);
        int priority = is.getInt("priority", 10);
        boolean purchasable = is.getBoolean("purchasable", true);
        String viewPermission = is.contains("view-permission") ? is.getString("view-permission") : null;
        String permission = is.contains("permission") ? is.getString("permission") : null;
        String negatePermission = is.contains("negate-permission")
            ? is.getString("negate-permission") : null;

        List<String> commands = is.contains("commands") ? is.getStringList("commands") : new ArrayList<>();
        List<String> lore = is.contains("lore") ? is.getStringList("lore") : new ArrayList<>();
        StoreIcon icon = StoreIcon.from(is.contains("icon") ? is.getSection("icon") : null);
        StoreItemDetails details = is.contains("details")
            ? loadDetails(key, name, is.getSection("details")) : null;

        if (purchasable && commands.isEmpty()) {
            AshStore.inst().getLogger().warning(
                "Store item '" + key + "' has no commands; it will do nothing when purchased.");
        }

        if (is.contains("slot") && (slot < 0 || slot >= 45)) {
            AshStore.inst().getLogger().warning(
                "Store item '" + key + "' has invalid slot " + slot + "; expected 0-44.");
            slot = -1;
        }

        return new StoreItem(key, name, price, slot, priority, purchasable, viewPermission,
            permission, negatePermission, commands, lore, icon, details);
    }

    private StoreItemDetails loadDetails(String itemKey, String itemName, Section ds) {
        String title = ds.getString("title", itemName);
        List<StoreItemDetailGroup> groups = new ArrayList<>();

        if (ds.contains("groups")) {
            Section groupSection = ds.getSection("groups");
            for (String groupKey : groupSection.getKeys()) {
                Section gs = groupSection.getSection(groupKey);
                if (gs == null) {
                    continue;
                }

                int slot = gs.getInt("slot", -1);
                if (gs.contains("slot") && (slot < 0 || slot >= 45)) {
                    AshStore.inst().getLogger().warning(
                        "Detail group '" + itemKey + "." + groupKey + "' has invalid slot "
                            + slot + "; expected 0-44. It will be placed automatically.");
                    slot = -1;
                }

                groups.add(new StoreItemDetailGroup(
                    groupKey,
                    gs.getString("name", groupKey),
                    slot,
                    gs.contains("lore") ? gs.getStringList("lore") : new ArrayList<>(),
                    StoreIcon.from(gs.contains("icon") ? gs.getSection("icon") : null)));
            }
        }

        int purchaseSlot = 49;
        String purchaseName = "<green>Purchase " + itemName;
        StoreIcon purchaseIcon = new StoreIcon(null, Material.LIME_CONCRETE);
        if (ds.contains("purchase")) {
            Section ps = ds.getSection("purchase");
            purchaseSlot = validMenuSlot(itemKey, "purchase", ps.getInt("slot", purchaseSlot), 49);
            purchaseName = ps.getString("name", purchaseName);
            if (ps.contains("icon")) {
                purchaseIcon = StoreIcon.from(ps.getSection("icon"));
            }
        }

        int backSlot = 45;
        if (ds.contains("back")) {
            Section bs = ds.getSection("back");
            backSlot = validMenuSlot(itemKey, "back", bs.getInt("slot", backSlot), 45);
        }
        if (purchaseSlot == backSlot) {
            AshStore.inst().getLogger().warning(
                "Store item '" + itemKey + "' uses slot " + backSlot
                    + " for both details buttons; purchase will use slot 49.");
            purchaseSlot = 49;
        }

        return new StoreItemDetails(title, groups, purchaseSlot, purchaseName, purchaseIcon, backSlot);
    }

    private int validMenuSlot(String itemKey, String button, int slot, int fallback) {
        if (slot >= 0 && slot < 54) {
            return slot;
        }
        AshStore.inst().getLogger().warning(
            "Store item '" + itemKey + "' has invalid details " + button + " slot "
                + slot + "; expected 0-53. Using " + fallback + ".");
        return fallback;
    }
}
