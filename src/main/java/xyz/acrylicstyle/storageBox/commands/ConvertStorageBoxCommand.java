package xyz.acrylicstyle.storageBox.commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import xyz.acrylicstyle.storageBox.utils.StorageBox;

import java.util.HashMap;
import java.util.Map;

public class ConvertStorageBoxCommand {
    public static final Map<Material, Material> materials = new HashMap<>();

    static {
        //materials.put(Material.STONE, Material.COBBLESTONE);
        //materials.put(Material.COBBLESTONE, Material.STONE);
    }

    public static void onCommand(Player player) {
        StorageBox storageBox = StorageBox.getStorageBox(player.getInventory().getItemInMainHand());
        if (storageBox == null) {
            player.sendMessage(ChatColor.RED + "現在手に持ってるアイテムはStorage Boxではありません。");
            player.sendMessage(ChatColor.RED + "Storage Boxを手に持ってからもう一度試してください。");
            return;
        }
        if (!materials.containsKey(storageBox.getType()) || storageBox.getTag() != null) {
            player.sendMessage(ChatColor.RED + "このStorage Boxは変換できません。");
            return;
        }
        Material material = materials.get(storageBox.getType());
        storageBox.setType(material);
        player.getInventory().setItemInMainHand(storageBox.getItemStack());
        player.sendMessage(ChatColor.GREEN + "アイテムの種類を" + ChatColor.RED + material.name().toLowerCase() + ChatColor.GREEN + "にしました。");
    }
}
