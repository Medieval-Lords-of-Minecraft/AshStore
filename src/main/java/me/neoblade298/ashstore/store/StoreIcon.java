package me.neoblade298.ashstore.store;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.neoblade298.neocore.bukkit.util.SkullUtil;
import me.neoblade298.neocore.shared.io.Section;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * Resolves a configured icon into an {@link ItemStack}.
 * Priority: base64 skull -> material -> STONE fallback.
 */
public class StoreIcon {

    private final String base64;
    private final Material material;

    public StoreIcon(String base64, Material material) {
        this.base64 = base64;
        this.material = material;
    }

    public static StoreIcon from(Section sec) {
        if (sec == null) {
            return new StoreIcon(null, null);
        }

        String base64 = sec.contains("base64") ? sec.getString("base64") : null;
        Material mat = null;
        if ((base64 == null || base64.isEmpty()) && sec.contains("material")) {
            String matName = sec.getString("material");
            try {
                mat = Material.valueOf(matName.toUpperCase());
            } catch (IllegalArgumentException e) {
                mat = null;
            }
        }
        return new StoreIcon(base64, mat);
    }

    public ItemStack build(Component name, List<Component> lore) {
        ItemStack item;
        if (base64 != null && !base64.isEmpty()) {
            ItemStack skull = null;
            try {
                skull = SkullUtil.fromBase64(base64);
            } catch (Exception ignored) {
                // fall through to stone below
            }
            item = skull != null ? skull : new ItemStack(Material.STONE);
        } else if (material != null) {
            item = new ItemStack(material);
        } else {
            item = new ItemStack(Material.STONE);
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (name != null) {
                meta.displayName(name.decoration(TextDecoration.ITALIC, false));
            }
            if (lore != null && !lore.isEmpty()) {
                meta.lore(lore.stream()
                        .map(c -> c.decoration(TextDecoration.ITALIC, false))
                        .toList());
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}
