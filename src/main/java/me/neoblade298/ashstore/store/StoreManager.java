package me.neoblade298.ashstore.store;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import me.neoblade298.ashstore.AshStore;
import me.neoblade298.neocore.bukkit.NeoCore;

/** Holds all loaded store categories and (re)loads them from the categories/ folder. */
public class StoreManager {

    private static final List<StoreCategory> categories = new ArrayList<>();

    public static void reload() {
        categories.clear();
        File folder = new File(AshStore.inst().getDataFolder(), "categories");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        NeoCore.loadFiles(folder, new StoreLoader());
        categories.sort(Comparator.comparingInt(StoreCategory::getPriority)
            .thenComparing(StoreCategory::getSortKey));
    }

    /** Called by {@link StoreLoader} for each loaded file. */
    public static void register(StoreCategory category) {
        categories.add(category);
    }

    public static List<StoreCategory> getCategories() {
        return categories;
    }
}
