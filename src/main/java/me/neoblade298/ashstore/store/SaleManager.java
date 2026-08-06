package me.neoblade298.ashstore.store;

import java.time.Duration;
import java.time.MonthDay;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import me.neoblade298.ashstore.AshStore;
import me.neoblade298.neocore.bukkit.NeoCore;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

/** Loads annual sales, calculates current prices, and announces sale starts. */
public final class SaleManager {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM-dd", Locale.ROOT);
    private static final Duration SCHEDULE_WINDOW = Duration.ofDays(1);
    private static final Duration MAX_RECHECK_DELAY = Duration.ofHours(1);

    private final AshStore plugin;
    private final ZoneId zone = ZoneId.systemDefault();
    private final ScheduledExecutorService executor;
    private volatile List<SalePeriod> sales = List.of();
    private ScheduledFuture<?> scheduledTask;
    private volatile long generation;

    public SaleManager(AshStore plugin) {
        this.plugin = plugin;
        this.executor = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "AshStore-Sales");
            thread.setDaemon(true);
            return thread;
        });
    }

    public synchronized void reload() {
        generation++;
        cancelScheduledTask();
        sales = loadSales();
        scheduleNextCheck(generation);
    }

    public long getPrice(long regularPrice) {
        int discount = sales.stream()
                .filter(sale -> sale.isActive(ZonedDateTime.now(zone)))
                .mapToInt(SalePeriod::discountPercent)
                .max()
                .orElse(0);
        return Math.max(0, Math.round(regularPrice * (100 - discount) / 100.0));
    }

    public synchronized void shutdown() {
        generation++;
        cancelScheduledTask();
        executor.shutdownNow();
    }

    private List<SalePeriod> loadSales() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("sales");
        if (section == null) {
            return List.of();
        }

        List<SalePeriod> loaded = new ArrayList<>();
        for (String id : section.getKeys(false)) {
            ConfigurationSection sale = section.getConfigurationSection(id);
            if (sale == null || !sale.getBoolean("enabled", true)) {
                continue;
            }
            try {
                MonthDay start = MonthDay.parse(sale.getString("start", ""), DATE_FORMAT);
                MonthDay end = MonthDay.parse(sale.getString("end", ""), DATE_FORMAT);
                int discount = sale.getInt("discount-percent", 0);
                if (!start.isValidYear(2023) || !end.isValidYear(2023)) {
                    plugin.getLogger().warning("Sale '" + id + "' uses February 29, which is not annual; skipping it.");
                    continue;
                }
                if (discount < 0 || discount > 100) {
                    plugin.getLogger().warning("Sale '" + id + "' has discount-percent outside 0-100; skipping it.");
                    continue;
                }
                String name = sale.getString("name", id);
                String message = sale.getString("start-message",
                        "<green><sale> has started! All store items are <discount>% off.");
                loaded.add(new SalePeriod(id, name, start, end, discount, message));
            } catch (DateTimeParseException ex) {
                plugin.getLogger().warning("Sale '" + id + "' has an invalid start or end date; expected MM-dd.");
            }
        }
        return List.copyOf(loaded);
    }

    private synchronized void scheduleNextCheck(long expectedGeneration) {
        if (expectedGeneration != generation || executor.isShutdown() || sales.isEmpty()) {
            return;
        }

        ZonedDateTime now = ZonedDateTime.now(zone);
        SaleStart next = sales.stream()
                .map(sale -> new SaleStart(sale, sale.nextStart(now)))
                .min(Comparator.comparing(SaleStart::time))
                .orElse(null);
        if (next == null) {
            return;
        }

        Duration untilStart = Duration.between(now, next.time());
        Duration delay;
        if (untilStart.compareTo(SCHEDULE_WINDOW) <= 0) {
            delay = untilStart;
        } else {
            delay = untilStart.minus(SCHEDULE_WINDOW);
            if (delay.compareTo(MAX_RECHECK_DELAY) > 0) {
                delay = MAX_RECHECK_DELAY;
            }
        }
        long delayMillis = Math.max(1, delay.toMillis());
        scheduledTask = executor.schedule(
                () -> handleScheduledCheck(next, expectedGeneration), delayMillis, TimeUnit.MILLISECONDS);
    }

    private void handleScheduledCheck(SaleStart expected, long expectedGeneration) {
        if (expectedGeneration != generation || executor.isShutdown() || !plugin.isEnabled()) {
            return;
        }

        ZonedDateTime now = ZonedDateTime.now(zone);
        if (!now.isBefore(expected.time())) {
            List<SalePeriod> starting = sales.stream()
                    .filter(sale -> sale.startAt(expected.time().getYear(), zone).equals(expected.time()))
                    .toList();
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (expectedGeneration == generation) {
                    starting.forEach(this::broadcastStart);
                }
            });
        }
        scheduleNextCheck(expectedGeneration);
    }

    private void broadcastStart(SalePeriod sale) {
        if (!plugin.isEnabled()) {
            return;
        }
        Bukkit.broadcast(NeoCore.miniMessage().deserialize(sale.startMessage(),
                Placeholder.unparsed("sale", sale.name()),
                Placeholder.unparsed("discount", Integer.toString(sale.discountPercent()))));
    }

    private void cancelScheduledTask() {
        if (scheduledTask != null) {
            scheduledTask.cancel(false);
            scheduledTask = null;
        }
    }

    private record SaleStart(SalePeriod sale, ZonedDateTime time) {
    }

    private record SalePeriod(String id, String name, MonthDay start, MonthDay end,
                              int discountPercent, String startMessage) {

        private boolean isActive(ZonedDateTime now) {
            ZonedDateTime startTime = startAt(now.getYear(), now.getZone());
            if (now.isBefore(startTime)) {
                startTime = startAt(now.getYear() - 1, now.getZone());
            }
            ZonedDateTime endTime = end.atYear(startTime.getYear()).atStartOfDay(now.getZone()).plusDays(1);
            if (end.isBefore(start)) {
                endTime = end.atYear(startTime.getYear() + 1).atStartOfDay(now.getZone()).plusDays(1);
            }
            return !now.isBefore(startTime) && now.isBefore(endTime);
        }

        private ZonedDateTime nextStart(ZonedDateTime now) {
            ZonedDateTime next = startAt(now.getYear(), now.getZone());
            return next.isAfter(now) ? next : startAt(now.getYear() + 1, now.getZone());
        }

        private ZonedDateTime startAt(int year, ZoneId zone) {
            return start.atYear(year).atStartOfDay(zone);
        }
    }
}