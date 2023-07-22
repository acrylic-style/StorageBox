package xyz.acrylicstyle.storageBox.utils;

import net.minecraft.nbt.NBTTagCompound;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.UUID;

public class StorageBox {
    private boolean autoCollect;
    private Material type;
    private long amount;

    public StorageBox(Material type, long amount) {
        this.type = type;
        this.amount = amount;
        this.autoCollect = true;
    }

    public StorageBox(Material type, long amount, boolean autoCollect) {
        this.type = type;
        this.amount = amount;
        this.autoCollect = autoCollect;
    }

    public static StorageBox getStorageBox(ItemStack itemStack) {
        try {
            NBTTagCompound tag = CraftItemStack.asNMSCopy(itemStack).w();
            if (!tag.e("storageBoxType")) {
                return null;
            }
            String s = tag.l("storageBoxType"); // getString
            Material type = Material.valueOf(s.equals("") || s.equals("null") ? "AIR" : s.toUpperCase());
            long amount = tag.i("storageBoxAmount"); // getLong
            boolean autoCollect = tag.q("storageBoxAutoCollect");
            return new StorageBox(type, amount, autoCollect);
        } catch (RuntimeException e) {
            return null;
        }
    }

    public static StorageBox getNewStorageBox() { return getNewStorageBox(null); }

    public static StorageBox getNewStorageBox(Material type) { return getNewStorageBox(type, 0); }

    public static StorageBox getNewStorageBox(Material type, long amount) { return new StorageBox(type, amount); }

    public ItemStack getItemStack() {
        ItemStack item = new ItemStack(getType() == null ? Material.BARRIER : getType());
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            throw new RuntimeException("ItemMeta is null");
        }
        String name;
        if (getType() == null) {
            name = "空";
        } else {
            name = getType().name().replaceAll("_", " ").toLowerCase();
            name = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
        }
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Storage Box " + ChatColor.YELLOW + "[" + ChatColor.WHITE + name + ChatColor.YELLOW + "]");
        meta.setLore(Arrays.asList(ChatColor.GRAY + "Amount: " + amount, ChatColor.GRAY + "AutoCollect: " + autoCollect));
        item.setItemMeta(meta);
        net.minecraft.world.item.ItemStack is = CraftItemStack.asNMSCopy(item);
        NBTTagCompound tag = is.w();
        tag.a("storageBoxType", this.type == null ? "null" : this.type.name());
        tag.a("storageBoxAmount", this.amount);
        tag.a("storageBoxAutoCollect", this.autoCollect);
        tag.a("randomUUID", UUID.randomUUID().toString());
        is.c(tag);
        return CraftItemStack.asBukkitCopy(is);
    }

    public void setAmount(long amount) { this.amount = amount; }

    public void increaseAmount() { setAmount(amount + 1); }

    public void decreaseAmount() { setAmount(amount - 1); }

    /**
     * Get material of this storage box.
     * @return Null if undefined, material otherwise.
     */
    public Material getType() { return type == null || type.isAir() ? null : type; }

    /**
     * Set material of this storage box.
     * @param type Null if undefined, material otherwise.
     */
    public void setType(Material type) { this.type = type; }

    public long getAmount() { return amount; }

    public boolean isEmpty() { return amount <= 0; }

    public boolean isAutoCollect() { return autoCollect; }

    public void setAutoCollect(boolean autoCollect) { this.autoCollect = autoCollect; }
}
