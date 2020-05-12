package xyz.acrylicstyle.storageBox.commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import xyz.acrylicstyle.storageBox.utils.StorageBox;
import xyz.acrylicstyle.tomeito_api.subcommand.PlayerSubCommandExecutor;
import xyz.acrylicstyle.tomeito_api.subcommand.SubCommand;

@SubCommand(name = "delete", usage = "/storage delete", description = "Storage Boxを削除します。")
public class DeleteCommand extends PlayerSubCommandExecutor {
    @Override
    public void onCommand(Player player, String[] args) {
        StorageBox storageBox = StorageBox.getStorageBox(player.getInventory().getItemInMainHand());
        if (storageBox == null) {
            player.sendMessage(ChatColor.RED + "現在手に持ってるアイテムはStorage Boxではありません。");
            player.sendMessage(ChatColor.RED + "Storage Boxを手に持ってからもう一度試してください。");
            return;
        }
        if (args.length == 0 || !args[0].equalsIgnoreCase("confirm")) {
            player.sendMessage(ChatColor.RED + "本当にStorage Boxを削除しますか？");
            player.sendMessage(ChatColor.RED + "中のアイテムは戻ってきません。");
            player.sendMessage(ChatColor.YELLOW + "/storage delete confirm" + ChatColor.GRAY + "で削除を確定できます。");
            return;
        }
        storageBox.delete();
        player.getInventory().setItemInMainHand(null);
        player.sendMessage(ChatColor.GREEN + "Storage Boxを削除しました。");
    }
}
