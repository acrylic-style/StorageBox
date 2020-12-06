package xyz.acrylicstyle.storageBox.commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import xyz.acrylicstyle.storageBox.utils.StorageBox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ChangeTypeCommand {
    private static final List<Material> WHITELIST = new ArrayList<>();

    static {
        WHITELIST.add(Material.COAL);
        WHITELIST.add(Material.CHARCOAL);
        WHITELIST.add(Material.DIAMOND);
        WHITELIST.add(Material.EMERALD);
        WHITELIST.add(Material.STICK);
        WHITELIST.add(Material.DEBUG_STICK);
        WHITELIST.add(Material.SUGAR);
        WHITELIST.add(Material.STRING);
        WHITELIST.add(Material.LAPIS_LAZULI);
        WHITELIST.add(Material.WHEAT_SEEDS);
        WHITELIST.add(Material.NETHERITE_INGOT);
        WHITELIST.addAll(Arrays.stream(Material.values()).filter(m -> m.name().endsWith("_DYE")).collect(Collectors.toList()));
        WHITELIST.addAll(Arrays.stream(Material.values()).filter(m -> m.name().endsWith("_INGOT")).collect(Collectors.toList()));
        WHITELIST.add(Material.REDSTONE);
    }

    public static void onCommand(Player player) {
        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (offHand.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "オフハンドに変更先のアイテムを持ってからもう一度実行してください。");
            return;
        }
        if (!WHITELIST.contains(offHand.getType()) && !offHand.getType().isBlock()) {
            player.sendMessage(ChatColor.RED + "Storage Boxに収納できるのはブロックのみです。");
            return;
        }
        if (StorageBox.getStorageBox(offHand) != null) {
            player.sendMessage(ChatColor.RED + "Storage BoxにStorage Boxを収納することはできません。");
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
        player.getInventory().setItemInMainHand(storageBox.getItemStack());
        player.sendMessage(ChatColor.GREEN + "Storage Boxのアイテムの種類を変更しました。");
    }
}
