package xyz.acrylicstyle.storageBox.commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import xyz.acrylicstyle.storageBox.utils.ItemUtil;
import xyz.acrylicstyle.storageBox.utils.StorageBox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ChangeTypeCommand {
    private static final List<Material> DISALLOW = new ArrayList<>();

    static {
        DISALLOW.addAll(Arrays.stream(Material.values()).filter(m -> m.name().endsWith("SHULKER_BOX")).collect(Collectors.toList()));
        DISALLOW.add(Material.ELYTRA);
    }

    public static void onCommand(Player player) {
        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (offHand.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "オフハンドに変更先のアイテムを持ってからもう一度実行してください。");
            return;
        }
        if (DISALLOW.contains(offHand.getType())) {
            player.sendMessage(ChatColor.RED + "このアイテムは格納できません。");
            return;
        }
        if (ItemUtil.getStringTag(offHand, "MYTHIC_TYPE").startsWith("ffggmesi")) {
            player.sendMessage(ChatColor.RED + "このアイテムは格納できません。");
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
        try {
            storageBox.importComponent(offHand);
        } catch (RuntimeException e) {
            player.getInventory().setItemInOffHand(offHand);
            player.sendMessage(ChatColor.RED + "エラーが発生しました。 (" + e.getMessage() + ")");
        }
        player.getInventory().setItemInMainHand(storageBox.getItemStack());
        player.sendMessage(ChatColor.GREEN + "Storage Boxのアイテムの種類を変更しました。");
    }
}
