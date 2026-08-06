package me.neoblade298.ashstore.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import me.neoblade298.ashstore.AshStore;
import me.neoblade298.ashstore.gui.CategoryMenu;
import me.neoblade298.ashstore.player.PlayerData;
import me.neoblade298.ashstore.player.PlayerManager;
import me.neoblade298.ashstore.store.StoreManager;
import me.neoblade298.neocore.bukkit.util.Util;

public class CmdAshStore implements CommandExecutor, TabCompleter {

    private static final List<String> SUBCOMMANDS = List.of("balance", "give", "take", "set", "reload");

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player p)) {
                Util.msgRaw(sender, "<red>Only players can open the store.");
                return true;
            }
            new CategoryMenu(p).openInventory();
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "balance", "bal" -> {
                if (!(sender instanceof Player p)) {
                    Util.msgRaw(sender, "<red>Only players have an AshCoins balance.");
                    return true;
                }
                PlayerData data = PlayerManager.get(p);
                long coins = data == null ? 0 : data.getCoins();
                Util.msgRaw(p, "<gold>Balance: <yellow>" + coins + "</yellow> AshCoins");
            }
            case "reload" -> {
                if (!sender.hasPermission("ashstore.admin")) {
                    Util.msgRaw(sender, "<red>You don't have permission to do that.");
                    return true;
                }
                AshStore.inst().reloadConfig();
                AshStore.inst().getSaleManager().reload();
                StoreManager.reload();
                Util.msgRaw(sender, "<green>Reloaded " + StoreManager.getCategories().size() + " store categorie(s).");
            }
            case "give", "take", "set" -> handleAdjust(sender, args);
            default -> Util.msgRaw(sender, "<red>Unknown subcommand. Use /ashstore for the store.");
        }
        return true;
    }

    private void handleAdjust(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ashstore.admin")) {
            Util.msgRaw(sender, "<red>You don't have permission to do that.");
            return;
        }
        if (args.length < 3) {
            Util.msgRaw(sender, "<red>Usage: /ashstore " + args[0].toLowerCase() + " <player> <amount>");
            return;
        }

        long amount;
        try {
            amount = Long.parseLong(args[2]);
        } catch (NumberFormatException ex) {
            Util.msgRaw(sender, "<red>'" + args[2] + "' is not a valid amount.");
            return;
        }

        String sub = args[0].toLowerCase();
        boolean set = sub.equals("set");
        long delta = sub.equals("take") ? -amount : amount;

        // Online + loaded: adjust in memory (persisted on the normal save cycle)
        Player online = Bukkit.getPlayerExact(args[1]);
        if (online != null) {
            PlayerData data = PlayerManager.get(online);
            if (data == null) {
                Util.msgRaw(sender, "<red>That player's data is still loading, try again shortly.");
                return;
            }
            if (set) {
                data.setCoins(amount);
            } else {
                data.addCoins(delta);
            }
            Util.msgRaw(sender, "<red>" + online.getName() + "<gray> now has <yellow>"
                    + data.getCoins() + " AshCoins</yellow>.");
            return;
        }

        // Offline: adjust directly in SQL, off the main thread
        OfflinePlayer offline = Bukkit.getOfflinePlayerIfCached(args[1]);
        if (offline == null) {
            Util.msgRaw(sender, "<red>No player named '" + args[1] + "' has joined this server before.");
            return;
        }
        UUID uuid = offline.getUniqueId();
        String name = offline.getName() != null ? offline.getName() : args[1];

        Bukkit.getScheduler().runTaskAsynchronously(AshStore.inst(), () -> {
            Long result = PlayerManager.adjustOffline(uuid, set ? amount : delta, set);
            Bukkit.getScheduler().runTask(AshStore.inst(), () -> {
                if (result == null) {
                    Util.msgRaw(sender, "<red>Failed to update " + name + "'s balance.");
                } else {
                    Util.msgRaw(sender, "<green>" + name + " now has <yellow>"
                            + result + "</yellow> AshCoins.");
                }
            });
        });
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            for (String sub : SUBCOMMANDS) {
                if (sub.startsWith(args[0].toLowerCase())) {
                    out.add(sub);
                }
            }
        } else if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("give") || sub.equals("take") || sub.equals("set")) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                        out.add(p.getName());
                    }
                }
            }
        }
        return out;
    }
}
