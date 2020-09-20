package xyz.acrylicstyle.storageBox.commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import util.Collection;
import xyz.acrylicstyle.storageBox.utils.StorageBox;
import xyz.acrylicstyle.tomeito_api.subcommand.PlayerOpSubCommandExecutor;
import xyz.acrylicstyle.tomeito_api.subcommand.SubCommand;

@SubCommand(name = "convert", usage = "/storage convert", description = "Storage Boxの種類を変換します。")
public class ConvertCommand extends PlayerOpSubCommandExecutor {
    public static final Collection<Material, Material> materials = new Collection<>();

    static {
        materials.add(Material.STONE, Material.COBBLESTONE);
        materials.add(Material.COBBLESTONE, Material.STONE);
    }
    
    @Override
    public void onCommand(Player player, String[] args) {
        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "アイテムの種類を指定してください。");
            return;
        }
        StorageBox storageBox = StorageBox.getStorageBox(player.getInventory().getItemInMainHand());
        if (storageBox == null) {
            player.sendMessage(ChatColor.RED + "現在手に持ってるアイテムはStorage Boxではありません。");
            player.sendMessage(ChatColor.RED + "Storage Boxを手に持ってからもう一度試してください。");
            return;
        }
        if (materials.containsKey(storageBox.getType())) {
            player.sendMessage(ChatColor.RED + "このStorage Boxは変換できません。");
            return;
        }
        Material material = materials.get(storageBox.getType());
        storageBox.setType(material);
        player.getInventory().setItemInMainHand(storageBox.getItemStack());
        player.sendMessage(ChatColor.GREEN + "アイテムの種類を" + ChatColor.RED + material.name().toLowerCase() + ChatColor.GREEN + "にしました。");
    }
}
