package me.neoblade298.ashstore.gui;

import java.util.List;
import java.util.Locale;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.ashstore.player.PlayerData;
import me.neoblade298.ashstore.player.PlayerManager;
import me.neoblade298.ashstore.store.StoreIcon;
import me.neoblade298.neocore.bukkit.NeoCore;
import net.kyori.adventure.text.Component;

/** Shared rendering for a player's AshCoin balance in store menus. */
final class BalanceDisplay {

    private BalanceDisplay() {
    }

    static ItemStack createIcon(Player player) {
        return new StoreIcon(null, Material.GOLD_NUGGET).build(
                NeoCore.miniMessage().deserialize("<gold>Your AshCoins"),
                List.of(balanceLine(player)));
    }

    static Component balanceLine(Player player) {
        PlayerData data = PlayerManager.get(player);
        if (data == null) {
            return NeoCore.miniMessage().deserialize("<gray>Balance unavailable");
        }
        return NeoCore.miniMessage().deserialize(
                "<yellow>" + format(data.getCoins()) + "</yellow> AshCoins");
    }

    static String format(long coins) {
        return String.format(Locale.US, "%,d", coins);
    }
}