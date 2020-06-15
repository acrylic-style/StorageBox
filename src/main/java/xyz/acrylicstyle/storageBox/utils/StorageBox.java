package xyz.acrylicstyle.storageBox.utils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.acrylicstyle.paper.Paper;
import xyz.acrylicstyle.paper.inventory.ItemStackUtils;
import xyz.acrylicstyle.paper.nbt.NBTBase;
import xyz.acrylicstyle.paper.nbt.NBTTagCompound;
import xyz.acrylicstyle.storageBox.StorageBoxPlugin;
import xyz.acrylicstyle.tomeito_api.utils.Log;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

public class StorageBox {
    private boolean autoCollect;
    @Nullable
    private Material type;
    private int amount;

    public StorageBox(@Nullable Material type, int amount) {
        this.type = type;
        this.amount = amount;
        this.autoCollect = true;
    }

    public StorageBox(@Nullable Material type, int amount, boolean autoCollect) {
        this.type = type;
        this.amount = amount;
        this.autoCollect = autoCollect;
    }

    @Nullable
    public static StorageBox getStorageBox(@NotNull ItemStack itemStack) {
        try {
            NBTTagCompound tag = Paper.itemStack(itemStack).getOrCreateTag();
            String uuid = tag.hasKey("uuid") ? Objects.requireNonNull(tag.get("uuid")).asString() : "";
            if (!uuid.equals("")) {
                itemStack = StorageBox.migrateStorageBox(itemStack, UUID.fromString(uuid)); // todo: Storage box - remove this later
                tag = Paper.itemStack(itemStack).getOrCreateTag();
            }
            if (!tag.hasKey("storageBoxType")) {
                return null;
            }
            String s = Objects.requireNonNull(tag.get("storageBoxType")).asString();
            Material type = Material.valueOf(s.equals("") || s.equals("null") ? "AIR" : s.toUpperCase());
            int amount = ((NBTBase.NBTNumber) Objects.requireNonNull(tag.get("storageBoxAmount"))).asInt();
            boolean autoCollect = tag.getBoolean("storageBoxAutoCollect");
            return new StorageBox(type, amount, autoCollect);
        } catch (RuntimeException e) {
            return null;
        }
    }

    // Storage Box start - todo: remove this later
    @Deprecated
    @NotNull
    public static ItemStack migrateStorageBox(@NotNull ItemStack item, @NotNull UUID uuid) {
        StorageBox storageBox = loadStorageBox(uuid);
        if (storageBox != null) {
            Log.info("Storage Box " + uuid.toString() + " was successfully migrated.");
            return storageBox.getItemStack();
        } else {
            return item;
        }
    }

    @Deprecated
    @Nullable
    private static StorageBox loadStorageBox(@NotNull UUID uuid) {
        if (StorageBoxPlugin.config.get("boxes." + uuid.toString()) == null) return null;
        Log.info("Migrating StorageBox: " + uuid.toString());
        boolean autoCollect = StorageBoxPlugin.config.getBoolean("boxes." + uuid.toString() + ".autoCollect", true);
        String _t = StorageBoxPlugin.config.getString("boxes." + uuid.toString() + ".type");
        Material type = _t == null ? null : Material.getMaterial(_t);
        int amount = StorageBoxPlugin.config.getInt("boxes." + uuid.toString() + ".amount", 0);
        StorageBoxPlugin.config.set("migratedBoxes." + uuid.toString(), StorageBoxPlugin.config.get("boxes." + uuid.toString()));
        return new StorageBox(type, amount, autoCollect);
    }
    // Storage Box end - remove this later

    @NotNull
    @Contract("-> new")
    public static StorageBox getNewStorageBox() { return getNewStorageBox(null); }

    @NotNull
    @Contract("_ -> new")
    public static StorageBox getNewStorageBox(@Nullable Material type) { return getNewStorageBox(type, 0); }

    @NotNull
    @Contract("_, _ -> new")
    public static StorageBox getNewStorageBox(@Nullable Material type, int amount) { return new StorageBox(type, amount); }

    @NotNull
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
        ItemStackUtils is = Paper.itemStack(item);
        NBTTagCompound tag = is.getOrCreateTag();
        tag.remove("uuid");
        tag.setString("storageBoxType", this.type == null ? "null" : this.type.name());
        tag.setInt("storageBoxAmount", this.amount);
        tag.setBoolean("storageBoxAutoCollect", this.autoCollect);
        tag.setString("randomUUID", UUID.randomUUID().toString());
        is.setTag(tag);
        return is.getItemStack();
    }

    public void setAmount(int amount) { this.amount = amount; }

    public void increaseAmount() { setAmount(amount + 1); }

    public void decreaseAmount() { setAmount(amount - 1); }

    /**
     * Get material of this storage box.
     * @return Null if undefined, material otherwise.
     */
    @Nullable
    public Material getType() { return type == null || type.isAir() ? null : type; }

    /**
     * Set material of this storage box.
     * @param type Null if undefined, material otherwise.
     */
    public void setType(@Nullable Material type) { this.type = type; }

    public int getAmount() { return amount; }

    public boolean isEmpty() { return amount <= 0; }

    public boolean isAutoCollect() { return autoCollect; }

    public void setAutoCollect(boolean autoCollect) { this.autoCollect = autoCollect; }
}
