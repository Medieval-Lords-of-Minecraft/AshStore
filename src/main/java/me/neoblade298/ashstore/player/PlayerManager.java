package me.neoblade298.ashstore.player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.bukkit.io.IOComponent;

public class PlayerManager implements IOComponent {

    public static final String KEY = "AshStore-Coins";

    private static final HashMap<UUID, PlayerData> data = new HashMap<>();

    public static PlayerData get(Player p) {
        return data.get(p.getUniqueId());
    }

    public static PlayerData get(UUID uuid) {
        return data.get(uuid);
    }

    /** Creates the coins table once, at startup (not per login). */
    public static void init() {
        try (Connection con = NeoCore.getConnection(KEY)) {
            if (con == null) {
                return;
            }
            try (Statement stmt = con.createStatement()) {
                stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS ashstore_coins (" +
                    "uuid VARCHAR(36) NOT NULL, " +
                    "coins BIGINT NOT NULL DEFAULT 0, " +
                    "PRIMARY KEY (uuid))"
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Adjusts an offline player's balance directly in SQL.
     *
     * @param set when true, sets the balance to {@code amount}; otherwise adds {@code amount} (may be negative).
     * @return the resulting balance, or {@code null} on failure.
     */
    public static Long adjustOffline(UUID uuid, long amount, boolean set) {
        try (Connection con = NeoCore.getConnection(KEY)) {
            if (con == null) {
                return null;
            }

            long current = 0;
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT coins FROM ashstore_coins WHERE uuid = ?")) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        current = rs.getLong("coins");
                    }
                }
            }

            long updated = set ? amount : current + amount;
            if (updated < 0) {
                updated = 0;
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "REPLACE INTO ashstore_coins (uuid, coins) VALUES (?, ?)")) {
                ps.setString(1, uuid.toString());
                ps.setLong(2, updated);
                ps.executeUpdate();
            }
            return updated;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void preloadPlayer(OfflinePlayer p, Statement stmt) {
        // Table creation is handled once in init(); nothing to do per login.
    }

    @Override
    public void loadPlayer(Player p, Statement stmt) {
        UUID uuid = p.getUniqueId();
        try {
            ResultSet rs = stmt.executeQuery(
                "SELECT * FROM ashstore_coins WHERE uuid = '" + uuid + "'"
            );

            if (rs.next()) {
                data.put(uuid, new PlayerData(p, rs));
            } else {
                data.put(uuid, new PlayerData(p));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void savePlayer(Player p, Connection con, List<PreparedStatement> stmts) throws Exception {
        UUID uuid = p.getUniqueId();
        PlayerData pd = data.get(uuid);
        if (pd != null) {
            stmts.add(pd.save(uuid, con));
        }
    }

    @Override
    public void cleanup(Connection con, List<PreparedStatement> stmts) throws Exception {
        data.clear();
    }
}
