package xyz.acrylicstyle.storageBox.utils;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.AbstractMap;
import java.util.Map;

public final class StorageBoxUtils {
    private StorageBoxUtils() {}

    public static ItemStack updateStorageBox(ItemStack itemStack) {
        StorageBox storageBox = StorageBox.getStorageBox(itemStack);
        if (storageBox == null) return itemStack;
        return storageBox.getItemStack();
    }

    public static Map.Entry<Integer, StorageBox> getStorageBoxForType(Inventory inventory, ItemStack item) {
        ItemStack[] c = inventory.getContents();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == null) continue;
            StorageBox box = StorageBox.getStorageBox(c[i]);
            if (box == null || !box.isAutoCollect()) continue;
            if (box.isComponentItemStackSimilar(item))
                return new AbstractMap.SimpleEntry<>(i, box);
        }
        return null;
    }
}
