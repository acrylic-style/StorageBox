package xyz.acrylicstyle.storageBox.commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import xyz.acrylicstyle.storageBox.utils.StorageBox;
import xyz.acrylicstyle.tomeito_api.subcommand.PlayerOpSubCommandExecutor;
import xyz.acrylicstyle.tomeito_api.subcommand.SubCommand;

import java.util.UUID;

@SubCommand(name = "getstorage", usage = "/storage getstorage", description = "指定されたUUIDに紐づけられているStorageBoxを取得します。[OP]")
public class GetStorageCommand extends PlayerOpSubCommandExecutor {
    @Override
    public void onCommand(Player player, String[] args) {
        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "UUIDを指定してください。");
            return;
        }
        UUID uuid;
        try {
            uuid = UUID.fromString(args[0]);
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "正しいUUIDを指定してください。");
            return;
        }
        StorageBox storageBox = StorageBox.loadStorageBox(uuid);
        player.getInventory().addItem(storageBox.getItemStack());
        player.sendMessage(ChatColor.GREEN + "StorageBoxを1個付与しました。");
    }
}
