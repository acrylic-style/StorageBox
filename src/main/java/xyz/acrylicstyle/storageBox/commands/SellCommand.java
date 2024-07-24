package xyz.acrylicstyle.storageBox.commands;

import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import xyz.acrylicstyle.storageBox.StorageBoxPlugin;
import xyz.acrylicstyle.storageBox.utils.StorageBox;

import java.util.Map;

public class SellCommand {
    public static void onCommand(Player player, String[] args) {
        StorageBox storageBox = StorageBox.getStorageBox(player.getInventory().getItemInMainHand());
        if (storageBox == null) {
            player.sendMessage(ChatColor.RED + "現在手に持ってるアイテムはStorage Boxではありません。");
            player.sendMessage(ChatColor.RED + "Storage Boxを手に持ってからもう一度試してください。");
            return;
        }
        ItemStack componentItemStack = storageBox.getComponentItemStack();
        long price =
                StorageBoxPlugin.getInstance()
                        .sellPrices
                        .entrySet()
                        .stream()
                        .filter(e -> e.getKey().isSimilar(componentItemStack))
                        .findAny()
                        .map(Map.Entry::getValue)
                        .orElse(0L);
        if (price == 0) {
            player.sendMessage(ChatColor.RED + "このアイテムは売れません。");
            return;
        }
        long amount;
        try {
            if (args.length == 0 || args[0].equalsIgnoreCase("all")) {
                amount = storageBox.getAmount();
            } else {
                amount = Long.parseLong(args[0]);
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
        storageBox.setAmount(storageBox.getAmount() - amount);
        player.getInventory().setItemInMainHand(storageBox.getItemStack());
        long money = amount * price;
        EconomyResponse response = StorageBoxPlugin.getEconomy().depositPlayer(player, money);
        if (response.transactionSuccess()) {
            player.sendMessage(ChatColor.GREEN + "アイテムを" + ChatColor.RED + amount + ChatColor.GREEN + "個売りました。");
            player.sendMessage("" + ChatColor.GREEN + money + "円が口座に入金されました。");
        } else {
            player.sendMessage(ChatColor.RED + "入金が失敗しました。 (" + response.errorMessage + ")");
        }
    }
}
