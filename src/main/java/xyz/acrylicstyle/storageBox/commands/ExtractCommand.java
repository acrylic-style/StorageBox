package xyz.acrylicstyle.storageBox.commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import xyz.acrylicstyle.storageBox.StorageBoxPlugin;
import xyz.acrylicstyle.storageBox.utils.StorageBox;

public class ExtractCommand {
    public static void onCommand(Player player, String[] args) {
        StorageBox storageBox = StorageBox.getStorageBox(player.getInventory().getItemInMainHand());
        if (storageBox == null) {
            player.sendMessage(ChatColor.RED + "現在手に持ってるアイテムはStorage Boxではありません。");
            player.sendMessage(ChatColor.RED + "Storage Boxを手に持ってからもう一度試してください。");
            return;
        }
        int amount;
        try {
            if (args.length == 0 || args[0].equalsIgnoreCase("all")) {
                amount = (int) Math.min(storageBox.getAmount(), StorageBoxPlugin.getEmptySlots(player) * 64L);
            } else {
                amount = Integer.parseInt(args[0]);
            }
        } catch (NumberFormatException ex) {
            player.sendMessage(ChatColor.RED + "数値を指定してください。");
            return;
        }
        if (amount < 0) {
            player.sendMessage(ChatColor.RED + "マイナスの値を指定することはできません。");
            return;
        }
        if (storageBox.getAmount() < amount) {
            player.sendMessage(ChatColor.RED + "Storage Boxに入っているアイテムが足りません。");
            return;
        }
        int i = (int) Math.ceil(amount / 64F);
        if (StorageBoxPlugin.getEmptySlots(player) >= i) {
            storageBox.setAmount(storageBox.getAmount() - amount);
            ItemStack[] items = new ItemStack[i];
            assert storageBox.getType() != null;
            for (int j = 0; j < i; j++) {
                items[j] = new ItemStack(storageBox.getType(), ((j+1) == i) && (amount % 64 != 0) ? amount % 64 : 64);
            }
            player.getInventory().addItem(items).values().forEach(is -> player.getWorld().dropItem(player.getLocation(), is));
            player.sendMessage(ChatColor.GREEN + "アイテムを" + ChatColor.RED + amount + ChatColor.GREEN + "個取り出しました。");
            player.getInventory().setItemInMainHand(storageBox.getItemStack());
        } else {
            player.sendMessage(ChatColor.RED + "インベントリの空きスペースが足りません。");
            player.sendMessage(ChatColor.GRAY + "(" + i + "個必要です。)");
        }
    }
}
