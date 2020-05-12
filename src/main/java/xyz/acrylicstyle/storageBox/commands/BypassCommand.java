package xyz.acrylicstyle.storageBox.commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import xyz.acrylicstyle.storageBox.StorageBoxPlugin;
import xyz.acrylicstyle.tomeito_api.subcommand.PlayerOpSubCommandExecutor;
import xyz.acrylicstyle.tomeito_api.subcommand.SubCommand;

@SubCommand(name = "bypass", usage = "/storage bypass", description = "アイテムチェックなどを無視します。[OP]")
public class BypassCommand extends PlayerOpSubCommandExecutor {
    @Override
    public void onCommand(Player player, String[] args) {
        if (StorageBoxPlugin.bypassingPlayers.contains(player.getUniqueId())) {
            StorageBoxPlugin.bypassingPlayers.remove(player.getUniqueId());
            player.sendMessage(ChatColor.GREEN + "チェックを無視しないようにしました。");
        } else {
            StorageBoxPlugin.bypassingPlayers.add(player.getUniqueId());
            player.sendMessage(ChatColor.GREEN + "チェックを無視するようにしました。");
        }
    }
}
