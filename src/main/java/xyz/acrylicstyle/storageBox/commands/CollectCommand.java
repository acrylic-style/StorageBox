package xyz.acrylicstyle.storageBox.commands;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import xyz.acrylicstyle.storageBox.utils.StorageBox;
import xyz.acrylicstyle.storageBox.utils.StorageBoxUtils;
import xyz.acrylicstyle.tomeito_api.command.PlayerCommandExecutor;
import xyz.acrylicstyle.tomeito_api.subcommand.SubCommand;

@SubCommand(name = "collect", usage = "/storage collect", description = "手に持ってるStorage Boxにインベントリに入ってるブロックを収納します。")
public class CollectCommand extends PlayerCommandExecutor {
    @Override
    public void onCommand(Player player, String[] args) {
        StorageBox storageBox = StorageBox.getStorageBox(player.getInventory().getItemInMainHand());
        if (fillTo(storageBox, player.getInventory())) return;
        player.getInventory().setItemInMainHand(StorageBoxUtils.updateStorageBox(player.getInventory().getItemInMainHand()));
    }

    public static boolean fillTo(StorageBox storageBox, Inventory inventory) {
        if (storageBox == null) return true;
        if (storageBox.getType() == null) return true;
        ItemStack[] c = inventory.getContents();
        for (int i = 0; i < c.length; i++) {
            ItemStack is = c[i];
            if (is == null) continue;
            if (StorageBox.getStorageBox(is) != null) continue;
            if (is.getType().equals(storageBox.getType())) {
                storageBox.setAmount(storageBox.getAmount() + is.getAmount());
                inventory.setItem(i, null);
            }
        }
        return false;
    }
}
