package me.neoblade298.ashstore.store;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;

import me.neoblade298.ashstore.AshStore;
import me.neoblade298.neocore.bukkit.NeoCore;

/** Holds all loaded store categories and (re)loads them from the categories/ folder. */
public class StoreManager {

    private static final List<StoreCategory> categories = new ArrayList<>();
    private static StoreMenuConfig rootMenu;

    public static void reload() {
        categories.clear();
        File folder = new File(AshStore.inst().getDataFolder(), "categories");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        NeoCore.loadFiles(folder, new StoreLoader());
        categories.sort(Comparator.comparingInt(StoreCategory::getPriority)
            .thenComparing(StoreCategory::getSortKey));
        rootMenu = loadRootMenu();
    }

    /** Called by {@link StoreLoader} for each loaded file. */
    public static void register(StoreCategory category) {
        categories.add(category);
    }

    public static List<StoreCategory> getCategories() {
        return categories;
    }

    public static StoreMenuConfig getRootMenu() {
        return rootMenu;
    }

    public static StoreCategory getCategory(String id) {
        return categories.stream()
                .filter(category -> category.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    private static StoreMenuConfig loadRootMenu() {
        ConfigurationSection menu = AshStore.inst().getConfig().getConfigurationSection("root-menu");
        String title = menu == null ? "<dark_gray>Store" : menu.getString("title", "<dark_gray>Store");
        int size = validSize(menu == null ? 27 : menu.getInt("size", 27));
        int infoSlot = validControlSlot(menu, "controls.info-slot", size, size - 5);
        int balanceSlot = validControlSlot(menu, "controls.balance-slot", size, size - 1);
        if (balanceSlot == infoSlot) {
            int fallback = infoSlot == size - 1 ? size - 2 : size - 1;
            AshStore.inst().getLogger().warning(
                    "Root menu info and balance controls share slot " + infoSlot
                            + "; balance will use slot " + fallback + ".");
            balanceSlot = fallback;
        }

        List<StoreMenuEntry> entries = new ArrayList<>();
        ConfigurationSection configuredEntries = menu == null
                ? null : menu.getConfigurationSection("entries");
        if (configuredEntries == null) {
            for (StoreCategory category : categories) {
                entries.add(new StoreMenuEntry.Category(
                        category, category.getSlot(), category.getPriority()));
            }
        } else {
            for (String key : configuredEntries.getKeys(false)) {
                ConfigurationSection entry = configuredEntries.getConfigurationSection(key);
                if (entry != null) {
                    loadRootEntry(key, entry, size, entries);
                }
            }
        }
        return new StoreMenuConfig(title, size, infoSlot, balanceSlot, List.copyOf(entries));
    }

    private static void loadRootEntry(String key, ConfigurationSection entry, int size,
                                      List<StoreMenuEntry> entries) {
        int slot = entry.getInt("slot", -1);
        if (slot < -1 || slot >= size) {
            AshStore.inst().getLogger().warning(
                    "Root menu entry '" + key + "' has invalid slot " + slot
                            + "; expected 0-" + (size - 1) + ". It will be placed automatically.");
            slot = -1;
        }
        int priority = entry.getInt("priority", 10);
        String type = entry.getString("type", "").toLowerCase();
        if (type.equals("category")) {
            String categoryId = entry.getString("category", "");
            StoreCategory category = getCategory(categoryId);
            if (category == null) {
                warnMissingEntry(key, "category", categoryId);
                return;
            }
            entries.add(new StoreMenuEntry.Category(category, slot, priority));
            return;
        }
        if (type.equals("item")) {
            String reference = entry.getString("item", "");
            int separator = reference.indexOf('.');
            StoreCategory category = separator < 1 ? null : getCategory(reference.substring(0, separator));
            StoreItem item = category == null ? null : category.getItem(reference.substring(separator + 1));
            if (item == null) {
                warnMissingEntry(key, "item", reference);
                return;
            }
            entries.add(new StoreMenuEntry.Item(item, slot, priority));
            return;
        }
        AshStore.inst().getLogger().warning(
                "Root menu entry '" + key + "' has unknown type '" + type
                        + "'; expected 'category' or 'item'.");
    }

    private static int validSize(int size) {
        if (size >= 9 && size <= 54 && size % 9 == 0) {
            return size;
        }
        AshStore.inst().getLogger().warning(
                "Root menu has invalid size " + size + "; expected 9, 18, 27, 36, 45, or 54. Using 27.");
        return 27;
    }

    private static int validControlSlot(ConfigurationSection menu, String path, int size, int fallback) {
        int slot = menu == null ? fallback : menu.getInt(path, fallback);
        if (slot >= 0 && slot < size) {
            return slot;
        }
        AshStore.inst().getLogger().warning(
                "Root menu '" + path + "' has invalid slot " + slot + "; using " + fallback + ".");
        return fallback;
    }

    private static void warnMissingEntry(String key, String type, String reference) {
        AshStore.inst().getLogger().warning(
                "Root menu entry '" + key + "' references unknown " + type + " '" + reference + "'.");
    }
}
