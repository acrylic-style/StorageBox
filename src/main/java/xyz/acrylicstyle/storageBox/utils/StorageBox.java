package xyz.acrylicstyle.storageBox.utils;

import net.minecraft.server.v1_17_R0.NBTNumber;
import net.minecraft.server.v1_17_R0.NBTTagCompound;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_17_R0.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.acrylicstyle.storageBox.StorageBoxPlugin;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

public class StorageBox {
    private boolean autoCollect;
    private Material type;
    private int amount;

    public StorageBox(Material type, int amount) {
        this.type = type;
        this.amount = amount;
        this.autoCollect = true;
    }

    public StorageBox(Material type, int amount, boolean autoCollect) {
        this.type = type;
        this.amount = amount;
        this.autoCollect = autoCollect;
    }

    public static StorageBox getStorageBox(ItemStack itemStack) {
        try {
            NBTTagCompound tag = CraftItemStack.asNMSCopy(itemStack).getOrCreateTag();
            String uuid = tag.hasKey("uuid") ? Objects.requireNonNull(tag.get("uuid")).asString() : "";
            if (!uuid.equals("")) {
                itemStack = StorageBox.migrateStorageBox(itemStack, UUID.fromString(uuid)); // todo: Storage box - remove this later
                tag = CraftItemStack.asNMSCopy(itemStack).getOrCreateTag();
                tag.remove("uuid");
            }
            if (!tag.hasKey("storageBoxType")) {
                return null;
            }
            String s = Objects.requireNonNull(tag.get("storageBoxType")).asString();
            Material type = Material.valueOf(s.equals("") || s.equals("null") ? "AIR" : s.toUpperCase());
            int amount = ((NBTNumber) Objects.requireNonNull(tag.get("storageBoxAmount"))).asInt();
            boolean autoCollect = tag.getBoolean("storageBoxAutoCollect");
            return new StorageBox(type, amount, autoCollect);
        } catch (RuntimeException e) {
            return null;
        }
    }

    // Storage Box start - todo: remove this later
    @Deprecated
    public static ItemStack migrateStorageBox(ItemStack item, UUID uuid) {
        StorageBox storageBox = loadStorageBox(uuid);
        if (storageBox != null) {
            //Log.info("Storage Box " + uuid.toString() + " was successfully migrated.");
            return storageBox.getItemStack();
        } else {
            return item;
        }
    }

    @Deprecated
    private static StorageBox loadStorageBox(UUID uuid) {
        if (StorageBoxPlugin.config.get("boxes." + uuid.toString()) == null) return null;
        //Log.info("Migrating StorageBox: " + uuid.toString());
        boolean autoCollect = StorageBoxPlugin.config.getBoolean("boxes." + uuid.toString() + ".autoCollect", true);
        String _t = StorageBoxPlugin.config.getString("boxes." + uuid.toString() + ".type");
        Material type = _t == null ? null : Material.getMaterial(_t);
        int amount = StorageBoxPlugin.config.getInt("boxes." + uuid.toString() + ".amount", 0);
        StorageBoxPlugin.config.set("migratedBoxes." + uuid.toString(), StorageBoxPlugin.config.get("boxes." + uuid.toString()));
        return new StorageBox(type, amount, autoCollect);
    }
    // Storage Box end - remove this later

    public static StorageBox getNewStorageBox() { return getNewStorageBox(null); }

    public static StorageBox getNewStorageBox(Material type) { return getNewStorageBox(type, 0); }

    public static StorageBox getNewStorageBox(Material type, int amount) { return new StorageBox(type, amount); }

    public ItemStack getItemStack() {
        ItemStack item = new ItemStack(getType() == null ? Material.BARRIER : getType());
        ItemMeta meta = item.getItemMeta();
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
        net.minecraft.server.v1_17_R0.ItemStack is = CraftItemStack.asNMSCopy(item);
        NBTTagCompound tag = is.getOrCreateTag();
        tag.remove("uuid");
        tag.setString("storageBoxType", this.type == null ? "null" : this.type.name());
        tag.setInt("storageBoxAmount", this.amount);
        tag.setBoolean("storageBoxAutoCollect", this.autoCollect);
        tag.setString("randomUUID", UUID.randomUUID().toString());
        is.setTag(tag);
        return CraftItemStack.asBukkitCopy(is);
    }

    public void setAmount(int amount) { this.amount = amount; }

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

    public int getAmount() { return amount; }

    public boolean isEmpty() { return amount <= 0; }

    public boolean isAutoCollect() { return autoCollect; }

    public void setAutoCollect(boolean autoCollect) { this.autoCollect = autoCollect; }
}
