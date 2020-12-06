package xyz.acrylicstyle.storageBox.commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import xyz.acrylicstyle.storageBox.StorageBoxPlugin;
import xyz.acrylicstyle.storageBox.utils.StorageBox;

public class NewCommand {
    public static void onCommand(Player player) {
        if (!StorageBoxPlugin.bypassingPlayers.contains(player.getUniqueId())) {
            if (!player.getInventory().contains(new ItemStack(Material.DIAMOND, 8)) || !player.getInventory().contains(Material.CHEST, 1)) {
                player.sendMessage(ChatColor.RED + "チェスト1個とダイヤ8個が必要です。");
                return;
            }
            player.getInventory().removeItem(new ItemStack(Material.DIAMOND, 8), new ItemStack(Material.CHEST, 1));
        }
        StorageBox storageBox = StorageBox.getNewStorageBox();
        player.getInventory().addItem(storageBox.getItemStack());
        player.sendMessage(ChatColor.GREEN + "新しいStorage Boxを作成しました。");
        player.sendMessage(ChatColor.GREEN + " - アイテムの種類を設定するには" + ChatColor.YELLOW + "/storage changetype" + ChatColor.GREEN + "を実行してください。");
        player.sendMessage(ChatColor.GREEN + " - 削除するには" + ChatColor.RED + "/storage delete" + ChatColor.GREEN + "を実行してください。");
        player.sendMessage(ChatColor.GREEN + " - その他の使い方などは" + ChatColor.YELLOW + "/storage" + ChatColor.GREEN + "を見てください。");
    }
}
