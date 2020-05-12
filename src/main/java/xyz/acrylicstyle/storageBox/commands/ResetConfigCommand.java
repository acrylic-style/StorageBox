package xyz.acrylicstyle.storageBox.commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import xyz.acrylicstyle.storageBox.StorageBoxPlugin;
import xyz.acrylicstyle.tomeito_api.subcommand.PlayerOpSubCommandExecutor;
import xyz.acrylicstyle.tomeito_api.subcommand.SubCommand;

@SubCommand(name = "resetconfig", usage = "/storage resetConfig", description = "設定をすべてリセットします。[OP]")
public class ResetConfigCommand extends PlayerOpSubCommandExecutor {
    @Override
    public void onCommand(Player player, String[] args) {
        StorageBoxPlugin.config.setThenSave("boxes", null);
        player.sendMessage(ChatColor.GREEN + "設定をリセットしました。");
    }
}
