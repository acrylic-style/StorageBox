package xyz.acrylicstyle.storageBox.commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import xyz.acrylicstyle.storageBox.utils.StorageBox;

public class CollectCommand {
    public static void onCommand(Player player) {
        StorageBox storageBox = StorageBox.getStorageBox(player.getInventory().getItemInMainHand());
        if (fillTo(storageBox, player.getInventory())) {
            player.sendMessage(ChatColor.RED + "Storage Boxを持っていないか、Storage Boxの種類が「空」です。");
            return;
        }
        player.getInventory().setItemInMainHand(storageBox.getItemStack());
        player.sendMessage(ChatColor.GREEN + "アイテムをすべてStorage Boxの中に収納しました。");
    }

    public static boolean fillTo(StorageBox storageBox, Inventory inventory) {
        if (storageBox == null) return true;
        if (storageBox.getType() == null) return true;
        ItemStack[] c = inventory.getContents();
        for (int i = 0; i < c.length; i++) {
            ItemStack is = c[i];
            if (is == null) continue;
            if (StorageBox.getStorageBox(is) != null) continue;
            if (storageBox.isComponentItemStackSimilar(is)) {
                storageBox.setAmount(storageBox.getAmount() + is.getAmount());
                inventory.setItem(i, null);
            }
        }
        return false;
    }
}
