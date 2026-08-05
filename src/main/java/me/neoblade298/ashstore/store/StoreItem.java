package me.neoblade298.ashstore.store;

import java.util.List;

/** A store menu item with optional purchase behavior and visibility requirements. */
public class StoreItem {

    private final String id;
    private final String name;
    private final long price;
    private final int slot;
    private final int priority;
    private final boolean purchasable;
    private final String viewPermission; // nullable
    private final String permission; // nullable
    private final String negatePermission; // nullable
    private final List<String> commands;
    private final List<String> lore;
    private final StoreIcon icon;
    private final StoreItemDetails details; // nullable

    public StoreItem(String id, String name, long price, int slot, int priority, boolean purchasable,
                     String viewPermission, String permission, String negatePermission,
                     List<String> commands, List<String> lore, StoreIcon icon,
                     StoreItemDetails details) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.slot = slot;
        this.priority = priority;
        this.purchasable = purchasable;
        this.viewPermission = viewPermission;
        this.permission = permission;
        this.negatePermission = negatePermission;
        this.commands = commands;
        this.lore = lore;
        this.icon = icon;
        this.details = details;
    }

    public String getId() {
        return id;
    }

    public int getSlot() {
        return slot;
    }

    public boolean hasSlot() {
        return slot >= 0;
    }

    /** Lower values win when multiple visible items target the same slot. */
    public int getPriority() {
        return priority;
    }

    public boolean isPurchasable() {
        return purchasable;
    }

    public String getViewPermission() {
        return viewPermission;
    }

    public boolean hasViewPermission() {
        return viewPermission != null && !viewPermission.isEmpty();
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

    public String getNegatePermission() {
        return negatePermission;
    }

    public boolean hasNegatePermission() {
        return negatePermission != null && !negatePermission.isEmpty();
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

    public boolean hasDetails() {
        return details != null;
    }

    public StoreItemDetails getDetails() {
        return details;
    }
}
