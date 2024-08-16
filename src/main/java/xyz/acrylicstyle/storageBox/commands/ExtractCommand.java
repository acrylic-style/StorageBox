package xyz.acrylicstyle.storageBox.commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import xyz.acrylicstyle.storageBox.StorageBoxPlugin;
import xyz.acrylicstyle.storageBox.utils.StorageBox;

import java.util.Objects;

public class ExtractCommand {
    public static void onCommand(Player player, String[] args) {
        StorageBox storageBox = StorageBox.getStorageBox(player.getInventory().getItemInMainHand());
        if (storageBox == null) {
            player.sendMessage(ChatColor.RED + "現在手に持ってるアイテムはStorage Boxではありません。");
            player.sendMessage(ChatColor.RED + "Storage Boxを手に持ってからもう一度試してください。");
            return;
        }
        int maxStackSize = storageBox.getComponentItemStack() == null ? 64 : storageBox.getComponentItemStack().getMaxStackSize();
        int amount;
        try {
            if (args.length == 0 || args[0].equalsIgnoreCase("all")) {
                amount = (int) Math.min(storageBox.getAmount() > 1 ? storageBox.getAmount() - 1 : 1,
                        StorageBoxPlugin.getEmptySlots(player) * (long) maxStackSize);
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
        int i = (int) Math.ceil(amount / (float) maxStackSize);
        if (StorageBoxPlugin.getEmptySlots(player) >= i) {
            ItemStack stack = storageBox.getComponentItemStack();
            storageBox.setAmount(storageBox.getAmount() - amount);
            ItemStack[] items = new ItemStack[i];
            for (int j = 0; j < i; j++) {
                ItemStack item = Objects.requireNonNull(stack).clone();
                item.setAmount(((j+1) == i) && (amount % maxStackSize != 0) ? amount % maxStackSize : maxStackSize);
                items[j] = item;
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
