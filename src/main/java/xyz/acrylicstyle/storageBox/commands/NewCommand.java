package xyz.acrylicstyle.storageBox.commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import xyz.acrylicstyle.storageBox.StorageBoxPlugin;
import xyz.acrylicstyle.storageBox.utils.StorageBox;
import xyz.acrylicstyle.tomeito_api.subcommand.PlayerSubCommandExecutor;
import xyz.acrylicstyle.tomeito_api.subcommand.SubCommand;
import xyz.acrylicstyle.tomeito_api.utils.Log;

@SubCommand(name = "new", usage = "/storage new", description = "新しいStorage Boxを作成します。")
public class NewCommand extends PlayerSubCommandExecutor {
    @Override
    public void onCommand(Player player, String[] args) {
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
        Log.info("Created new storage box by " + player.getName() + ".");
    }
}
