package xyz.acrylicstyle.storageBox.commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import xyz.acrylicstyle.storageBox.utils.StorageBox;
import xyz.acrylicstyle.tomeito_api.subcommand.PlayerOpSubCommandExecutor;
import xyz.acrylicstyle.tomeito_api.subcommand.SubCommand;
import xyz.acrylicstyle.tomeito_api.utils.TypeUtil;

@SubCommand(name = "setamount", usage = "/storage setamount [amount]", description = "アイテムの数を設定します。[OP]")
public class SetAmountCommand extends PlayerOpSubCommandExecutor {
    @Override
    public void onCommand(Player player, String[] args) {
        if (args.length == 0 || !TypeUtil.isInt(args[0])) {
            player.sendMessage(ChatColor.RED + "数字を指定してください。");
            return;
        }
        StorageBox storageBox = StorageBox.getStorageBox(player.getInventory().getItemInMainHand());
        if (storageBox == null) {
            player.sendMessage(ChatColor.RED + "現在手に持ってるアイテムはStorage Boxではありません。");
            player.sendMessage(ChatColor.RED + "Storage Boxを手に持ってからもう一度試してください。");
            return;
        }
        int amount = Integer.parseInt(args[0]);
        storageBox.setAmount(amount);
        player.getInventory().setItemInMainHand(storageBox.getItemStack());
        player.sendMessage(ChatColor.GREEN + "アイテムの数を" + ChatColor.RED + amount + ChatColor.GREEN + "にしました。");
    }
}
