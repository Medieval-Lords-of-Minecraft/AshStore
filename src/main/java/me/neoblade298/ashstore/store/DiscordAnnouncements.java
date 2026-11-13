package me.neoblade298.ashstore.store;

import org.bukkit.Bukkit;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.util.DiscordUtil;
import me.neoblade298.ashstore.AshStore;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public final class DiscordAnnouncements {

    private DiscordAnnouncements() {}

    public static void announcePurchase(String player, String item) {
        if (!Bukkit.getPluginManager().isPluginEnabled("DiscordSRV")) {
            return;
        }

        AshStore plugin = AshStore.inst();
        String template = plugin.getConfig().getString("messages.discord-broadcast",
                "<player> just purchased <item> from /store!");
        if (template == null || template.isBlank()) {
            return;
        }
        if (!DiscordSRV.isReady) {
            plugin.getLogger().warning("Could not announce purchase to Discord: DiscordSRV is not ready.");
            return;
        }

        String channelName = plugin.getConfig().getString("discord.channel", "").trim();
        var discord = DiscordSRV.getPlugin();
        var channel = channelName.isEmpty() ? discord.getMainTextChannel()
                : discord.getDestinationTextChannelForGameChannelName(channelName);
        if (channel == null) {
            plugin.getLogger().warning("Could not announce purchase to Discord: no channel found for "
                    + (channelName.isEmpty() ? "DiscordSRV's main channel" : "'" + channelName + "'") + ".");
            return;
        }

        var message = MiniMessage.miniMessage().deserialize(template,
                Placeholder.unparsed("player", player),
                Placeholder.parsed("item", item));
        String plain = PlainTextComponentSerializer.plainText().serialize(message);
        if (plain.isBlank()) {
            plugin.getLogger().warning("Could not announce purchase to Discord: message contains no text.");
            return;
        }
        DiscordUtil.sendMessage(channel, DiscordUtil.escapeMarkdown(plain));
    }
}
