package me.neoblade298.ashstore.store;

import java.util.List;

/** Layout and resolved entries for the root store menu. */
public record StoreMenuConfig(
        String title,
        int size,
        int infoSlot,
        int balanceSlot,
        List<StoreMenuEntry> entries) {
}