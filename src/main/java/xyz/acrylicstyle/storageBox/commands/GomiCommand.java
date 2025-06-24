package xyz.acrylicstyle.storageBox.commands;

import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import xyz.acrylicstyle.storageBox.StorageBoxPlugin;
import xyz.acrylicstyle.storageBox.utils.StorageBox;

import java.util.Map;

public class GomiCommand {
    public static void onCommand(Player player, String[] args) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "何も持っていません。");
            return;
        }
        StorageBox storageBox = StorageBox.getStorageBox(item);
        if (storageBox == null) {
            player.sendMessage(ChatColor.RED + "StorageBoxではありません。");
            return;
        }
        
        // Check if item is sellable
        ItemStack componentItemStack = storageBox.getComponentItemStack();
        long sellPrice = StorageBoxPlugin.getInstance()
                .sellPrices
                .entrySet()
                .stream()
                .filter(e -> e.getKey().isSimilar(componentItemStack))
                .findAny()
                .map(Map.Entry::getValue)
                .orElse(0L);
        
        boolean isSellable = sellPrice > 0;
        
        // Parse amount
        long amount;
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("all")) {
                amount = storageBox.getAmount();
            } else {
                try {
                    amount = Long.parseLong(args[0]);
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "無効な数値です: " + args[0]);
                    return;
                }
            }
        } else {
            amount = storageBox.getAmount();
        }
        
        if (amount <= 0) {
            player.sendMessage(ChatColor.RED + "数量は1以上である必要があります。");
            return;
        }
        
        if (amount > storageBox.getAmount()) {
            player.sendMessage(ChatColor.RED + "StorageBoxにそれだけのアイテムがありません。");
            return;
        }
        
        // Handle sellable items with bypass option
        if (isSellable && (args.length <= 1 || !args[args.length - 1].equalsIgnoreCase("confirm"))) {
            double totalValue = sellPrice * amount;
            
            player.sendMessage("");
            player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "警告: " + ChatColor.YELLOW + "このアイテムは売却可能です！");
            player.sendMessage(ChatColor.GRAY + "売却価格: " + ChatColor.GREEN + "$" + String.format("%.2f", totalValue) + ChatColor.GRAY + " (" + amount + "個)");
            player.sendMessage(ChatColor.RED + "本当に捨てますか？ " + ChatColor.GOLD + ChatColor.UNDERLINE + "/sb gomi " + amount + " confirm" + ChatColor.RED + " で確認");
            player.sendMessage("");
            return;
        }
        
        // Calculate disposal cost
        double disposalCost = amount * 1.0; // $1 per item
        
        // Check if player has enough money
        if (StorageBoxPlugin.getEconomy().getBalance(player) < disposalCost) {
            player.sendMessage(ChatColor.RED + "お金が足りません。必要: $" + String.format("%.2f", disposalCost));
            return;
        }
        
        // Withdraw money from player
        EconomyResponse response = StorageBoxPlugin.getEconomy().withdrawPlayer(player, disposalCost);
        if (!response.transactionSuccess()) {
            player.sendMessage(ChatColor.RED + "支払いが失敗しました。 (" + response.errorMessage + ")");
            return;
        }
        
        // Remove items from StorageBox
        storageBox.setAmount(storageBox.getAmount() - amount);
        
        // Update item in hand (always keep the StorageBox)
        player.getInventory().setItemInMainHand(storageBox.getItemStack());
        
        // Send success message
        player.sendMessage(ChatColor.GREEN + "" + amount + "個のアイテムを捨てました。" + ChatColor.GRAY + " (費用: $" + String.format("%.2f", disposalCost) + ")");
    }
}