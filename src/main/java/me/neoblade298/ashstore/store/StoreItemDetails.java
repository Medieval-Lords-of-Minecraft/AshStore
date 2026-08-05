package me.neoblade298.ashstore.store;

import java.util.List;

/** Optional configuration for an item's expanded perk menu. */
public class StoreItemDetails {

    private final String title;
    private final List<StoreItemDetailGroup> groups;
    private final int purchaseSlot;
    private final String purchaseName;
    private final StoreIcon purchaseIcon;
    private final int backSlot;

    public StoreItemDetails(String title, List<StoreItemDetailGroup> groups,
                            int purchaseSlot, String purchaseName,
                            StoreIcon purchaseIcon, int backSlot) {
        this.title = title;
        this.groups = groups;
        this.purchaseSlot = purchaseSlot;
        this.purchaseName = purchaseName;
        this.purchaseIcon = purchaseIcon;
        this.backSlot = backSlot;
    }

    public String getTitle() {
        return title;
    }

    public List<StoreItemDetailGroup> getGroups() {
        return groups;
    }

    public int getPurchaseSlot() {
        return purchaseSlot;
    }

    public String getPurchaseName() {
        return purchaseName;
    }

    public StoreIcon getPurchaseIcon() {
        return purchaseIcon;
    }

    public int getBackSlot() {
        return backSlot;
    }
}