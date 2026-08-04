package me.neoblade298.ashstore;

import java.io.File;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import me.neoblade298.ashstore.commands.CmdAshStore;
import me.neoblade298.ashstore.player.PlayerManager;
import me.neoblade298.ashstore.store.StoreManager;
import me.neoblade298.neocore.bukkit.NeoCore;

public class AshStore extends JavaPlugin {

    private static AshStore inst;

    public static AshStore inst() {
        return inst;
    }

    @Override
    public void onEnable() {
        inst = this;

        // Register IO component for AshCoins load/save, then create the table once
        NeoCore.registerIOComponent(this, new PlayerManager(), PlayerManager.KEY);
        PlayerManager.init();

        // Copy the default category on first run, then load all categories
        saveDefaultCategory();
        StoreManager.reload();

        // Register commands
        initCommands();

        getLogger().info("AshStore enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("AshStore disabled!");
    }

    private void saveDefaultCategory() {
        File folder = new File(getDataFolder(), "categories");
        File[] categories = folder.listFiles((dir, name) -> {
            String lowerName = name.toLowerCase();
            return lowerName.endsWith(".yml") || lowerName.endsWith(".yaml");
        });
        if (categories == null || categories.length == 0) {
            saveResource("categories/example.yml", false);
        }
    }

    private void initCommands() {
        PluginCommand cmd = getCommand("ashstore");
        CmdAshStore executor = new CmdAshStore();
        cmd.setExecutor(executor);
        cmd.setTabCompleter(executor);
    }
}
