package me.neoblade298.ashstore.gui;

import java.util.ArrayList;
import java.util.List;

import me.neoblade298.ashstore.AshStore;
import me.neoblade298.ashstore.store.SaleManager.PriceDetails;
import me.neoblade298.neocore.bukkit.NeoCore;
import net.kyori.adventure.text.Component;

/** Shared sale-price formatting for store item lore and purchase dialogs. */
final class PriceDisplay {

    private PriceDisplay() {
    }

    static PriceDetails getDetails(long regularPrice) {
        return AshStore.inst().getSaleManager().getPriceDetails(regularPrice);
    }

    static List<Component> lore(PriceDetails price) {
        List<Component> lines = new ArrayList<>();
        if (price.discountPercent() > 0) {
            lines.add(NeoCore.miniMessage().deserialize(
                    "<gold>Sale price: <yellow>" + BalanceDisplay.format(price.salePrice())
                            + "</yellow> AshCoins"));
            lines.add(NeoCore.miniMessage().deserialize(
                    "<gray>Regular price: <strikethrough>"
                            + BalanceDisplay.format(price.regularPrice())
                            + "</strikethrough> AshCoins"));
            lines.add(NeoCore.miniMessage().deserialize(
                    "<green>" + price.discountPercent() + "% off"));
        } else {
            lines.add(NeoCore.miniMessage().deserialize(
                    "<gold>Price: <yellow>" + BalanceDisplay.format(price.salePrice())
                            + "</yellow> AshCoins"));
        }
        return lines;
    }

    static List<Component> confirmation(PriceDetails price) {
        List<Component> lines = new ArrayList<>();
        lines.add(NeoCore.miniMessage().deserialize(
                "<gray>Purchase for <yellow>" + BalanceDisplay.format(price.salePrice())
                        + "</yellow> AshCoins?"));
        if (price.discountPercent() > 0) {
            lines.add(NeoCore.miniMessage().deserialize(
                    "<green>" + price.discountPercent() + "% off"
                            + " <gray>(regularly <strikethrough>"
                            + BalanceDisplay.format(price.regularPrice())
                            + "</strikethrough> AshCoins)"));
        }
        return lines;
    }
}
