package xyz.acrylicstyle.storageBox.utils;

import net.minecraft.server.v1_17_R0.NBTTagCompound;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_17_R0.inventory.CraftItemStack;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class StorageBoxUtils {
    private StorageBoxUtils() {}

    public static ItemStack updateStorageBox(ItemStack itemStack) {
        net.minecraft.server.v1_17_R0.ItemStack item = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = item.getOrCreateTag();
        String s = tag.hasKey("uuid") ? Objects.requireNonNull(tag.get("uuid")).asString() : "";
        if (!s.equals("")) itemStack = StorageBox.migrateStorageBox(itemStack, UUID.fromString(s)); // Storage box - todo: remove this later
        StorageBox storageBox = StorageBox.getStorageBox(itemStack);
        if (storageBox == null) return itemStack;
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

    public static Map.Entry<Integer, StorageBox> getStorageBoxForType(Inventory inventory, ItemStack item) {
        ItemStack[] c = inventory.getContents();
        for (int i = 0; i < c.length; i++) {
            StorageBox box = StorageBox.getStorageBox(c[i]);
            if (box == null || !box.isAutoCollect()) continue;
            if (box.getType() != null && new ItemStack(box.getType()).isSimilar(item))
                return new AbstractMap.SimpleEntry<>(i, box);
        }
        return null;
    }
}
