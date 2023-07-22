package xyz.acrylicstyle.storageBox.commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import xyz.acrylicstyle.storageBox.utils.StorageBox;

public class SetTypeCommand {
    public static void onCommand(Player player, String[] args) {
        if (!player.hasPermission("storagebox.op")) {
            return;
        }
        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "アイテムの種類を指定してください。");
            return;
        }
        StorageBox storageBox = StorageBox.getStorageBox(player.getInventory().getItemInMainHand());
        if (storageBox == null) {
            player.sendMessage(ChatColor.RED + "現在手に持ってるアイテムはStorage Boxではありません。");
            player.sendMessage(ChatColor.RED + "Storage Boxを手に持ってからもう一度試してください。");
            return;
        }
        Material material = Material.valueOf(args[0].toUpperCase());
        storageBox.setType(material);
        player.getInventory().setItemInMainHand(storageBox.getItemStack());
        player.sendMessage(ChatColor.GREEN + "アイテムの種類を" + ChatColor.RED + material.name().toLowerCase() + ChatColor.GREEN + "にしました。");
    }
}
