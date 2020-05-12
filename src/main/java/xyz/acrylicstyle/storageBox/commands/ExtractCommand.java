package xyz.acrylicstyle.storageBox.commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import xyz.acrylicstyle.storageBox.StorageBoxPlugin;
import xyz.acrylicstyle.storageBox.utils.StorageBox;
import xyz.acrylicstyle.storageBox.utils.StorageBoxUtils;
import xyz.acrylicstyle.tomeito_api.subcommand.PlayerSubCommandExecutor;
import xyz.acrylicstyle.tomeito_api.subcommand.SubCommand;
import xyz.acrylicstyle.tomeito_api.utils.TypeUtil;

@SubCommand(name = "extract", usage = "/storage extract [amount]", description = "アイテムをStorage Boxから取り出します。")
public class ExtractCommand extends PlayerSubCommandExecutor {
    @Override
    public void onCommand(Player player, String[] args) {
        if (args.length == 0 || !TypeUtil.isInt(args[0])) {
            player.sendMessage(ChatColor.RED + "Amountに数字を指定してください。");
            return;
        }
        StorageBox storageBox = StorageBox.getStorageBox(player.getInventory().getItemInMainHand());
        if (storageBox == null) {
            player.sendMessage(ChatColor.RED + "現在手に持ってるアイテムはStorage Boxではありません。");
            player.sendMessage(ChatColor.RED + "Storage Boxを手に持ってからもう一度試してください。");
            return;
        }
        int amount = Integer.parseInt(args[0]);
        if (amount < 0) {
            player.sendMessage(ChatColor.RED + "マイナスの値を指定することはできません。");
            return;
        }
        if ((storageBox.getAmount() - amount) < 0) {
            player.sendMessage(ChatColor.RED + "Storage Boxに入っているアイテムが足りません。");
            return;
        }
        int i = (int) Math.ceil(amount / 64F);
        if (StorageBoxPlugin.getEmptySlots(player) >= i) {
            storageBox.setAmount(storageBox.getAmount() - amount);
            ItemStack[] items = new ItemStack[i];
            assert storageBox.getType() != null;
            for (int j = 0; j < i; j++) {
                items[j] = new ItemStack(storageBox.getType(), (j+1) == i ? amount % 64 : 64);
            }
            player.getInventory().addItem(items);
            player.sendMessage(ChatColor.GREEN + "アイテムを" + ChatColor.RED + amount + ChatColor.GREEN + "個取り出しました。");
            player.getInventory().setItemInMainHand(StorageBoxUtils.updateStorageBox(player.getInventory().getItemInMainHand()));
        } else {
            player.sendMessage(ChatColor.RED + "インベントリの空きスペースが足りません。");
            player.sendMessage(ChatColor.GRAY + "(" + i + "個必要です。)");
        }
    }
}
