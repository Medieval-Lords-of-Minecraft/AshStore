package me.neoblade298.ashstore.store;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** A logical folder of {@link StoreItem}s. */
public class StoreCategory {

    private final String id;
    private final String name;
    private final int slot;
    private final int priority;
    private final String sortKey;
    private final StoreIcon icon;
    private final List<StoreItem> items = new ArrayList<>();

    public StoreCategory(String id, String name, int slot, int priority, StoreIcon icon) {
        this.id = id;
        this.name = name;
        this.slot = slot;
        this.priority = priority;
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

    public int getSlot() {
        return slot;
    }

    public boolean hasSlot() {
        return slot >= 0;
    }

    public int getPriority() {
        return priority;
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
