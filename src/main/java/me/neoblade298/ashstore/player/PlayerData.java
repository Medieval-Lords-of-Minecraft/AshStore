package me.neoblade298.ashstore.player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import me.neoblade298.neocore.shared.util.SQLInsertBuilder;
import me.neoblade298.neocore.shared.util.SQLInsertBuilder.SQLAction;

public class PlayerData {

    private long coins;

    /** New player with default values */
    public PlayerData() {
        this.coins = 0;
    }

    /** Load from SQL result */
    public PlayerData(ResultSet rs) throws SQLException {
        this.coins = rs.getLong("coins");
    }

    public long getCoins() {
        return coins;
    }

    public void setCoins(long coins) {
        this.coins = Math.max(0, coins);
    }

    public void addCoins(long amount) {
        setCoins(this.coins + amount);
    }

    public boolean canAfford(long amount) {
        return coins >= amount;
    }

    /** Deducts the amount if affordable. Returns true on success. */
    public boolean deduct(long amount) {
        if (!canAfford(amount)) {
            return false;
        }
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
