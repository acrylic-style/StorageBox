package xyz.acrylicstyle.storageBox.commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import xyz.acrylicstyle.storageBox.utils.StorageBox;
import xyz.acrylicstyle.storageBox.utils.StorageBoxUtils;
import xyz.acrylicstyle.tomeito_api.subcommand.PlayerSubCommandExecutor;
import xyz.acrylicstyle.tomeito_api.subcommand.SubCommand;

@SubCommand(name = "changetype", usage = "/storage changetype", description = "StorageBoxのアイテムの中身を変えます。オフハンドに変更先のアイテムを持ってください。")
public class ChangeTypeCommand extends PlayerSubCommandExecutor {
    @Override
    public void onCommand(Player player, String[] args) {
        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (offHand.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "オフハンドに変更先のアイテムを持ってからもう一度実行してください。");
            return;
        }
        if (!offHand.getType().isBlock()) {
            player.sendMessage(ChatColor.RED + "Storage Boxに収納できるのはブロックのみです。");
            return;
        }
        if (StorageBox.getStorageBox(offHand) != null) {
            player.sendMessage(ChatColor.RED + "Storage BoxにStorage Boxを格納することはできません。");
            return;
        }
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        StorageBox storageBox = StorageBox.getStorageBox(mainHand);
        if (storageBox == null) {
            player.sendMessage(ChatColor.RED + "現在手に持ってるアイテムはStorage Boxではありません。");
            player.sendMessage(ChatColor.RED + "Storage Boxを手に持ってからもう一度試してください。");
            return;
        }
        if (!storageBox.isEmpty()) {
            player.sendMessage(ChatColor.RED + "Storage Boxにアイテムが入っています。");
            player.sendMessage(ChatColor.RED + "アイテムを空にしてからもう一度実行してください。");
            return;
        }
        player.getInventory().setItemInOffHand(null);
        storageBox.setType(offHand.getType());
        storageBox.setAmount(offHand.getAmount());
        player.getInventory().setItemInMainHand(StorageBoxUtils.updateStorageBox(mainHand));
        player.sendMessage(ChatColor.GREEN + "Storage Boxのアイテムの種類を変更しました。");
    }
}
