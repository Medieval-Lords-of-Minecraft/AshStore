package me.neoblade298.ashstore.gui;

import java.util.List;

import org.bukkit.entity.Player;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.DialogInstancesProvider;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import me.neoblade298.ashstore.AshStore;
import me.neoblade298.ashstore.player.PlayerData;
import me.neoblade298.ashstore.player.PlayerManager;
import me.neoblade298.ashstore.store.StoreItem;
import me.neoblade298.neocore.bukkit.NeoCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;

/** Displays the standard native Paper confirmation dialog for a purchase. */
public final class PurchaseConfirmationDialog {

    private PurchaseConfirmationDialog() {
    }

    public static void show(Player player, StoreItem item, Runnable confirmAction) {
        DialogInstancesProvider dialogs = DialogInstancesProvider.instance();
        long price = AshStore.inst().getSaleManager().getPrice(item.getPrice());
        PlayerData data = PlayerManager.get(player);
        ClickCallback.Options callbackOptions = ClickCallback.Options.builder()
                .uses(1)
                .build();

        ActionButton confirm = ActionButton.builder(Component.text("Confirm Purchase"))
                .tooltip(Component.text("Spend " + price + " AshCoins"))
                .action(DialogAction.customClick((response, audience) -> confirmAction.run(), callbackOptions))
                .build();
        ActionButton cancel = ActionButton.builder(Component.text("Cancel")).build();

        List<DialogBody> body = new java.util.ArrayList<>();
        body.add(DialogBody.plainMessage(NeoCore.miniMessage().deserialize(item.getName())));
        body.add(DialogBody.plainMessage(NeoCore.miniMessage().deserialize(
                "<gray>Purchase for <yellow>" + BalanceDisplay.format(price) + "</yellow> AshCoins?")));
        body.add(DialogBody.plainMessage(BalanceDisplay.balanceLine(player)));
        if (data != null) {
            if (data.canAfford(price)) {
                body.add(DialogBody.plainMessage(NeoCore.miniMessage().deserialize(
                        "<gray>Balance after purchase: <yellow>"
                                + BalanceDisplay.format(data.getCoins() - price) + "</yellow> AshCoins")));
            } else {
                body.add(DialogBody.plainMessage(NeoCore.miniMessage().deserialize(
                        "<red>You do not have enough AshCoins.")));
            }
        }

        DialogBase base = dialogs.dialogBaseBuilder(Component.text("Confirm Purchase"))
                .externalTitle(Component.text("Confirm Purchase"))
                .canCloseWithEscape(true)
                .body(body)
                .build();

        Dialog dialog = Dialog.create(factory -> factory.empty()
                .base(base)
                .type(dialogs.confirmation(cancel, confirm)));
        player.showDialog(dialog);
    }
}