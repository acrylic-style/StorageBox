package xyz.acrylicstyle.storageBox.commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import xyz.acrylicstyle.storageBox.StorageBoxPlugin;
import xyz.acrylicstyle.storageBox.gui.ShopScreen;

public class ShopCommand {
    public static void onCommand(Player player) {
        if (StorageBoxPlugin.getInstance().buyPrices.isEmpty()) {
            player.sendMessage(ChatColor.RED + "このコマンドは現在使用できません。");
            return;
        }
        player.openInventory(new ShopScreen(player, StorageBoxPlugin.getInstance().buyPrices).getInventory());
    }
}
