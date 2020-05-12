package xyz.acrylicstyle.storageBox.utils;

import net.minecraft.server.v1_15_R1.NBTTagCompound;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.UUID;

public final class StorageBoxUtils {
    private StorageBoxUtils() {}

    public static ItemStack updateStorageBox(ItemStack itemStack) {
        net.minecraft.server.v1_15_R1.ItemStack i = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = i.getOrCreateTag();
        String s = tag.getString("uuid");
        i.setTag(tag);
        itemStack = CraftItemStack.asBukkitCopy(i);
        if (s == null) throw new IllegalArgumentException("This item isn't storage box");
        StorageBox storageBox = StorageBox.loadStorageBox(UUID.fromString(s));
        itemStack.setType(storageBox.getType() == null ? Material.BARRIER : storageBox.getType());
        ItemMeta meta = itemStack.getItemMeta();
        String name;
        if (storageBox.getType() == null) {
            name = "空";
        } else {
            name = storageBox.getType().name().replaceAll("_", " ").toLowerCase();
            name = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
        }
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Storage Box " + ChatColor.YELLOW + "[" + ChatColor.WHITE + name + ChatColor.YELLOW + "]");
        meta.setLore(Arrays.asList(ChatColor.GRAY + "Amount: " + storageBox.getAmount(), ChatColor.GRAY + "AutoCollect: " + storageBox.isAutoCollect()));
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    public static StorageBox getStorageBoxForType(@NotNull Inventory inventory, @NotNull Material type) {
        for (ItemStack item : inventory.getContents()) {
            StorageBox box = StorageBox.getStorageBox(item);
            if (box == null || !box.isAutoCollect()) continue;
            if (box.getType() == type) return box;
        }
        return null;
    }
}
