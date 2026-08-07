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
        ClickCallback.Options callbackOptions = ClickCallback.Options.builder()
                .uses(1)
                .build();

        ActionButton confirm = ActionButton.builder(Component.text("Confirm Purchase"))
                .tooltip(Component.text("Spend " + price + " AshCoins"))
                .action(DialogAction.customClick((response, audience) -> confirmAction.run(), callbackOptions))
                .build();
        ActionButton cancel = ActionButton.builder(Component.text("Cancel")).build();

        DialogBase base = dialogs.dialogBaseBuilder(Component.text("Confirm Purchase"))
                .externalTitle(Component.text("Confirm Purchase"))
                .canCloseWithEscape(true)
                .body(List.of(
                    DialogBody.plainMessage(NeoCore.miniMessage().deserialize(item.getName())),
                    DialogBody.plainMessage(NeoCore.miniMessage().deserialize(
                                                "<gray>Purchase for <yellow>" + price + "</yellow> AshCoins?"))))
                .build();

        Dialog dialog = Dialog.create(factory -> factory.empty()
                .base(base)
                .type(dialogs.confirmation(cancel, confirm)));
        player.showDialog(dialog);
    }
}