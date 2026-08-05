package me.neoblade298.ashstore.store;

import java.util.List;

/** A keyed group of related perks displayed in an item's details menu. */
public class StoreItemDetailGroup {

    private final String id;
    private final String name;
    private final int slot;
    private final List<String> lore;
    private final StoreIcon icon;

    public StoreItemDetailGroup(String id, String name, int slot, List<String> lore, StoreIcon icon) {
        this.id = id;
        this.name = name;
        this.slot = slot;
        this.lore = lore;
        this.icon = icon;
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

    public List<String> getLore() {
        return lore;
    }

    public StoreIcon getIcon() {
        return icon;
    }
}