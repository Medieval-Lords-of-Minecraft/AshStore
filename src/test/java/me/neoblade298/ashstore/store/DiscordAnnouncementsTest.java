package me.neoblade298.ashstore.store;

import static org.mockito.Mockito.*;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.util.DiscordUtil;
import me.neoblade298.ashstore.AshStore;

class DiscordAnnouncementsTest {

    private final AshStore plugin = mock(AshStore.class);
    private final PluginManager plugins = mock(PluginManager.class);
    private final DiscordSRV discord = mock(DiscordSRV.class);
    private final TextChannel channel = mock(TextChannel.class);
    private final Logger logger = mock(Logger.class);
    private final YamlConfiguration config = new YamlConfiguration();
    private MockedStatic<Bukkit> bukkit;
    private MockedStatic<AshStore> ashStore;
    private MockedStatic<DiscordSRV> discordSrv;
    private MockedStatic<DiscordUtil> discordUtil;
    private boolean wasReady;

    @BeforeEach
    void setUp() {
        bukkit = mockStatic(Bukkit.class);
        ashStore = mockStatic(AshStore.class);
        discordSrv = mockStatic(DiscordSRV.class);
        discordSrv.when(DiscordSRV::getPlugin).thenReturn(discord);
        discordUtil = mockStatic(DiscordUtil.class);
        bukkit.when(Bukkit::getPluginManager).thenReturn(plugins);
        ashStore.when(AshStore::inst).thenReturn(plugin);
        when(plugins.isPluginEnabled("DiscordSRV")).thenReturn(true);
        when(plugin.getConfig()).thenReturn(config);
        when(plugin.getLogger()).thenReturn(logger);
        when(discord.getMainTextChannel()).thenReturn(channel);
        discordUtil.when(() -> DiscordUtil.escapeMarkdown(anyString())).thenCallRealMethod();
        wasReady = DiscordSRV.isReady;
        DiscordSRV.isReady = true;
    }

    @AfterEach
    void tearDown() {
        DiscordSRV.isReady = wasReady;
        discordUtil.close();
        discordSrv.close();
        ashStore.close();
        bukkit.close();
    }

    @Test
    void sendsPlainEscapedPurchaseToMainChannel() {
        DiscordAnnouncements.announcePurchase("Test_Player", "<gold>VIP</gold>");

        discordUtil.verify(() -> DiscordUtil.sendMessage(channel,
                "Test\\_Player just purchased VIP from /store!"));
    }

    @Test
    void usesConfiguredChannelAndMessageIndependentlyOfGameBroadcast() {
        config.set("messages.broadcast", "");
        config.set("messages.discord-broadcast", "<green><player> bought <item>!</green>");
        config.set("discord.channel", " purchases ");
        when(discord.getDestinationTextChannelForGameChannelName("purchases")).thenReturn(channel);

        DiscordAnnouncements.announcePurchase("Steve", "<gold>VIP</gold>");

        discordUtil.verify(() -> DiscordUtil.sendMessage(channel, "Steve bought VIP!"));
        verify(discord, never()).getMainTextChannel();
    }

    @Test
    void skipsWhenDiscordSrvIsAbsent() {
        when(plugins.isPluginEnabled("DiscordSRV")).thenReturn(false);

        DiscordAnnouncements.announcePurchase("Steve", "VIP");

        discordSrv.verifyNoInteractions();
        discordUtil.verifyNoInteractions();
    }

    @Test
    void skipsWhenMessageIsDisabled() {
        config.set("messages.discord-broadcast", "");

        DiscordAnnouncements.announcePurchase("Steve", "VIP");

        discordSrv.verifyNoInteractions();
        discordUtil.verifyNoInteractions();
    }

    @Test
    void logsWhenDiscordSrvIsNotReady() {
        DiscordSRV.isReady = false;

        DiscordAnnouncements.announcePurchase("Steve", "VIP");

        verify(logger).warning("Could not announce purchase to Discord: DiscordSRV is not ready.");
        discordUtil.verifyNoInteractions();
    }

    @Test
    void logsMissingChannelWithoutSendingElsewhere() {
        config.set("discord.channel", "missing");

        DiscordAnnouncements.announcePurchase("Steve", "VIP");

        verify(logger).warning("Could not announce purchase to Discord: no channel found for 'missing'.");
        verify(discord, never()).getMainTextChannel();
        discordUtil.verifyNoInteractions();
    }

    @Test
    void logsFormattingOnlyMessageWithoutSending() {
        config.set("messages.discord-broadcast", "<gold></gold>");

        DiscordAnnouncements.announcePurchase("Steve", "VIP");

        verify(logger).warning("Could not announce purchase to Discord: message contains no text.");
        discordUtil.verifyNoInteractions();
    }
}
