package xyz.acrylicstyle.storageBox.commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import xyz.acrylicstyle.storageBox.StorageBoxPlugin;
import xyz.acrylicstyle.storageBox.utils.StorageBox;

public class NewCommand {
    public static void onCommand(Player player) {
        if (StorageBoxPlugin.getInstance().getConfig().getBoolean("disable-crafting", false)) {
            player.sendMessage(ChatColor.RED + "このコマンドは無効化されています。");
            return;
        }
        if (!StorageBoxPlugin.bypassingPlayers.contains(player.getUniqueId())) {
            if (!hasAtLeast(player, Material.DIAMOND, 8) || !hasAtLeast(player, Material.CHEST, 1)) {
                player.sendMessage(ChatColor.RED + "チェスト1個とダイヤ8個が必要です。");
                return;
            }
            player.getInventory().removeItem(new ItemStack(Material.DIAMOND, 8), new ItemStack(Material.CHEST, 1));
        }
        StorageBox storageBox = StorageBox.getNewStorageBox();
        player.getInventory().addItem(storageBox.getItemStack());
        player.sendMessage(ChatColor.GREEN + "新しいStorage Boxを作成しました。");
        player.sendMessage(ChatColor.GREEN + " - アイテムの種類を設定するには、設定したいものをオフハンドに持ったうえで" + ChatColor.YELLOW + "/sb changetype" + ChatColor.GREEN + "を実行してください。");
        player.sendMessage(ChatColor.GREEN + " - アイテムを取り出すには" + ChatColor.YELLOW + "/sb extract <数>" + ChatColor.GREEN + "を実行してください。");
        player.sendMessage(ChatColor.GREEN + " - 自動収集をオフにするには" + ChatColor.YELLOW + "/sb autocollect" + ChatColor.GREEN + "を実行してください。");
        player.sendMessage(ChatColor.GREEN + " - その他の使い方などは" + ChatColor.YELLOW + "/sb" + ChatColor.GREEN + "を見てください。");
    }

    @SuppressWarnings("ConstantValue") // In some version of spigot, the item stack might be null
    private static boolean hasAtLeast(Player player, Material material, int amount) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                count += item.getAmount();
                if (count >= amount) {
                    return true;
                }
            }
        }
        return false;
    }
}
