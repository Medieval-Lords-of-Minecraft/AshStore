package me.neoblade298.ashstore.store;

import java.util.List;

/** A single purchasable store item: one or more commands, a price, an optional permission, and an icon. */
public class StoreItem {

    private final String id;
    private final String name;
    private final long price;
    private final int priority;
    private final String permission; // nullable
    private final List<String> commands;
    private final List<String> lore;
    private final StoreIcon icon;

    public StoreItem(String id, String name, long price, int priority, String permission,
                     List<String> commands, List<String> lore, StoreIcon icon) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.priority = priority;
        this.permission = permission;
        this.commands = commands;
        this.lore = lore;
        this.icon = icon;
    }

    public String getId() {
        return id;
    }

    /** Manual sort order, 1 = highest priority (shown first), 10 = lowest. */
    public int getPriority() {
        return priority;
    }

    public String getName() {
        return name;
    }

    public long getPrice() {
        return price;
    }

    public String getPermission() {
        return permission;
    }

    public boolean hasPermission() {
        return permission != null && !permission.isEmpty();
    }

    public List<String> getCommands() {
        return commands;
    }

    public List<String> getLore() {
        return lore;
    }

    public StoreIcon getIcon() {
        return icon;
    }
}
