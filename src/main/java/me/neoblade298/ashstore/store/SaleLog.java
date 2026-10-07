package me.neoblade298.ashstore.store;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.neoblade298.ashstore.AshStore;
import me.neoblade298.ashstore.player.PlayerManager;
import me.neoblade298.neocore.bukkit.NeoCore;

/** Persists a record of every completed purchase (who bought what, and when) to SQL. */
public final class SaleLog {

    private SaleLog() {
    }

    /** Creates the sales table once, at startup. */
    public static void init() {
        try (Connection con = NeoCore.getConnection(PlayerManager.KEY)) {
            if (con == null) {
                return;
            }
            try (Statement stmt = con.createStatement()) {
                stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS ashstore_sales (" +
                    "id INT NOT NULL AUTO_INCREMENT, " +
                    "uuid VARCHAR(36) NOT NULL, " +
                    "player_name VARCHAR(16) NOT NULL, " +
                    "item_id VARCHAR(64) NOT NULL, " +
                    "item_name VARCHAR(128) NOT NULL, " +
                    "price BIGINT NOT NULL, " +
                    "purchased_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "PRIMARY KEY (id))"
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Asynchronously records a completed purchase. Safe to call from the main thread. */
    public static void log(Player player, StoreItem item, long price) {
        String uuid = player.getUniqueId().toString();
        String name = player.getName();
        String itemId = item.getId();
        String itemName = item.getName();

        Bukkit.getScheduler().runTaskAsynchronously(AshStore.inst(), () -> {
            try (Connection con = NeoCore.getConnection(PlayerManager.KEY)) {
                if (con == null) {
                    return;
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO ashstore_sales (uuid, player_name, item_id, item_name, price) "
                                + "VALUES (?, ?, ?, ?, ?)")) {
                    ps.setString(1, uuid);
                    ps.setString(2, name);
                    ps.setString(3, itemId);
                    ps.setString(4, itemName);
                    ps.setLong(5, price);
                    ps.executeUpdate();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
