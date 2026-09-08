package me.neoblade298.ashstore.store;

/** A category or purchasable item placed in the root store menu. */
public sealed interface StoreMenuEntry permits StoreMenuEntry.Category, StoreMenuEntry.Item {

    int slot();

    int priority();

    record Category(StoreCategory category, int slot, int priority) implements StoreMenuEntry {
    }

    record Item(StoreItem item, int slot, int priority) implements StoreMenuEntry {
    }
}