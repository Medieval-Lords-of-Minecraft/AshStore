package me.neoblade298.ashstore.store;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
            StoreIcon icon = StoreIcon.from(cfg.contains("icon") ? cfg.getSection("icon") : null);
            StoreCategory category = new StoreCategory(id, name, icon);

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
            permission, negatePermission, commands, lore, icon);
    }
}
