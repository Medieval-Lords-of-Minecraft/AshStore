package me.neoblade298.ashstore.player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.util.SQLInsertBuilder;
import me.neoblade298.neocore.shared.util.SQLInsertBuilder.SQLAction;

public class PlayerData {

    private final Player player;
    private long coins;

    /** New player with default values */
    public PlayerData(Player player) {
        this.player = player;
        this.coins = 0;
    }

    /** Load from SQL result */
    public PlayerData(Player player, ResultSet rs) throws SQLException {
        this.player = player;
        this.coins = rs.getLong("coins");
    }

    public Player getPlayer() {
        return player;
    }

    public long getCoins() {
        return coins;
    }

    public void setCoins(long coins) {
        this.coins = Math.max(0, coins);
    }

    public void addCoins(long amount) {
        setCoins(this.coins + amount);
		String symbol = amount > 0 ? "+" : "";
		Util.msgRaw(player, "<yellow>" + symbol + amount + " AshCoins </yellow>(<gold>" + coins + "</gold>)");
    }

    public boolean canAfford(long amount) {
        return coins >= amount;
    }

    /** Deducts the amount if affordable. Returns true on success. */
    public boolean deduct(long amount) {
        if (!canAfford(amount)) {
            return false;
        }
        Util.msgRaw(player, "<yellow>-" + amount + " AshCoins </yellow>(<gold>" + coins + "</gold>)");
        coins -= amount;
        return true;
    }

    public PreparedStatement save(UUID uuid, Connection con) throws SQLException {
        return new SQLInsertBuilder(SQLAction.REPLACE, "ashstore_coins")
                .addValue("uuid", uuid.toString())
                .addValue("coins", coins)
                .build(con);
    }
}
