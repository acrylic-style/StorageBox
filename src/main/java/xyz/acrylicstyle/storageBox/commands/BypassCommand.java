package xyz.acrylicstyle.storageBox.commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import xyz.acrylicstyle.storageBox.StorageBoxPlugin;

public class BypassCommand {
    public static void onCommand(Player player) {
        if (!player.hasPermission("storagebox.op")) {
            return;
        }
        if (StorageBoxPlugin.bypassingPlayers.contains(player.getUniqueId())) {
            StorageBoxPlugin.bypassingPlayers.remove(player.getUniqueId());
            player.sendMessage(ChatColor.GREEN + "チェックを無視しないようにしました。");
        } else {
            StorageBoxPlugin.bypassingPlayers.add(player.getUniqueId());
            player.sendMessage(ChatColor.GREEN + "チェックを無視するようにしました。");
        }
    }
}
