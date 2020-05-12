package xyz.acrylicstyle.storageBox.commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import xyz.acrylicstyle.storageBox.utils.StorageBox;
import xyz.acrylicstyle.storageBox.utils.StorageBoxUtils;
import xyz.acrylicstyle.tomeito_api.subcommand.PlayerSubCommandExecutor;
import xyz.acrylicstyle.tomeito_api.subcommand.SubCommand;

@SubCommand(name = "autocollect", usage = "/storage autocollect", description = "アイテムの自動回収を切り替えます。")
public class AutoCollectCommand extends PlayerSubCommandExecutor {
    @Override
    public void onCommand(Player player, String[] args) {
        StorageBox storageBox = StorageBox.getStorageBox(player.getInventory().getItemInMainHand());
        if (storageBox == null) {
            player.sendMessage(ChatColor.RED + "現在手に持ってるアイテムはStorage Boxではありません。");
            player.sendMessage(ChatColor.RED + "Storage Boxを手に持ってからもう一度試してください。");
            return;
        }
        boolean b = storageBox.isAutoCollect();
        storageBox.setAutoCollect(!b);
        player.getInventory().setItemInMainHand(StorageBoxUtils.updateStorageBox(player.getInventory().getItemInMainHand()));
        player.sendMessage(ChatColor.GREEN + "Auto Collectを" + ChatColor.YELLOW + (!b) + ChatColor.GREEN + "にしました。");
    }
}
