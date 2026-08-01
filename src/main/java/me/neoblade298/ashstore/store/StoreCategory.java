package me.neoblade298.ashstore.store;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** A logical folder of {@link StoreItem}s. Categories are sorted alphabetically by display name. */
public class StoreCategory {

    private final String id;
    private final String name;
    private final String sortKey;
    private final StoreIcon icon;
    private final List<StoreItem> items = new ArrayList<>();

    public StoreCategory(String id, String name, StoreIcon icon) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        // Strip MiniMessage tags so sorting is based on the visible text.
        this.sortKey = name.replaceAll("<[^>]+>", "").trim().toLowerCase();
    }

    public void addItem(StoreItem item) {
        items.add(item);
    }

    /** Preserves priority ordering for categories whose items do not configure slots. */
    public void sortItems() {
        items.sort(Comparator.comparingInt(StoreItem::getPriority));
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSortKey() {
        return sortKey;
    }

    public StoreIcon getIcon() {
        return icon;
    }

    public List<StoreItem> getItems() {
        return items;
    }
}
