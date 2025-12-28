package xyz.acrylicstyle.storageBox.utils;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.component.CustomData;
import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemUtil {
    public static @Nullable CompoundTag getCustomData(@Nullable ItemStack stack) {
        if (stack == null || stack.getType().isAir()) return null;
        CustomData customData = CraftItemStack.asNMSCopy(stack).get(DataComponents.CUSTOM_DATA);
        if (customData == null) return null;
        return customData.copyTag();
    }

    public static @NotNull ItemStack createItem(@NotNull Material material, @NotNull String displayName, @NotNull List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(displayName);
        if (!lore.isEmpty()) {
            meta.setLore(lore);
        }
        item.setItemMeta(meta);
        return item;
    }

    public static @NotNull String getStringTag(@NotNull ItemStack item, @NotNull String key) {
        CompoundTag tag = getCustomData(item);
        if (tag == null) return "";
        return tag.getString(key).orElse("");
    }
}
