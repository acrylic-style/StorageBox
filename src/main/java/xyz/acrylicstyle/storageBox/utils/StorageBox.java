package xyz.acrylicstyle.storageBox.utils;

import net.minecraft.server.v1_15_R1.NBTTagCompound;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.acrylicstyle.storageBox.StorageBoxPlugin;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class StorageBox {
    private static final Set<Material> opaqueExempt = new HashSet<>(Arrays.asList(
            Material.COAL, Material.CHARCOAL, Material.DIAMOND, Material.EMERALD, Material.STICK, Material.DEBUG_STICK,
            Material.SUGAR, Material.STRING, Material.LAPIS_LAZULI, Material.WHEAT_SEEDS, Material.REDSTONE,
            Material.GLOWSTONE_DUST, Material.RED_MUSHROOM, Material.BROWN_MUSHROOM
    ));

    static {
        opaqueExempt.addAll(Arrays.stream(Material.values()).filter(m -> m.name().endsWith("_DYE")).collect(Collectors.toList()));
        opaqueExempt.addAll(Arrays.stream(Material.values()).filter(m -> m.name().endsWith("_INGOT")).collect(Collectors.toList()));
    }

    private boolean autoCollect;
    private Material type;
    private long amount;
    private final @Nullable UUID randomUUID;

    public StorageBox(Material type, long amount) {
        this.type = type;
        this.amount = amount;
        this.autoCollect = true;
        this.randomUUID = null;
    }

    public StorageBox(Material type, long amount, boolean autoCollect, @Nullable UUID randomUUID) {
        this.type = type;
        this.amount = amount;
        this.autoCollect = autoCollect;
        this.randomUUID = randomUUID;
    }

    public static StorageBox getStorageBox(ItemStack itemStack) {
        try {
            NBTTagCompound tag = CraftItemStack.asNMSCopy(itemStack).getOrCreateTag();
            if (!tag.hasKey("storageBoxType")) {
                return null;
            }
            String s = tag.getString("storageBoxType");
            Material type = Material.valueOf(s.isEmpty() || s.equals("null") ? "AIR" : s.toUpperCase());
            long amount = tag.getLong("storageBoxAmount");
            boolean autoCollect = tag.getBoolean("storageBoxAutoCollect");
            UUID randomUUID = UUID.fromString(tag.getString("randomUUID"));
            return new StorageBox(type, amount, autoCollect, randomUUID);
        } catch (RuntimeException e) {
            return null;
        }
    }

    public static StorageBox getNewStorageBox() { return getNewStorageBox(null); }

    public static StorageBox getNewStorageBox(Material type) { return getNewStorageBox(type, 0); }

    public static StorageBox getNewStorageBox(Material type, long amount) { return new StorageBox(type, amount); }

    public ItemStack getItemStack() {
        Material itemType = getType() == null ? Material.BARRIER : getType();
        if (!itemType.isBlock() && !opaqueExempt.contains(itemType)) {
            itemType = Material.STICK;
        }
        ItemStack item = new ItemStack(itemType);
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
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Storage Box " + ChatColor.YELLOW + "[" + ChatColor.WHITE + name + ChatColor.YELLOW + "] " + ChatColor.GRAY + "<" + this.amount + ">");
        String id = randomUUID != null ? randomUUID.toString() : UUID.randomUUID().toString();
        meta.setLore(Arrays.asList(ChatColor.GRAY + "Amount: " + amount, ChatColor.GRAY + "AutoCollect: " + autoCollect, ChatColor.GRAY + "ID: " + id));
        meta.setCustomModelData(StorageBoxPlugin.customModelData);
        if (amount > 0) {
            meta.addEnchant(Enchantment.BINDING_CURSE, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        item.setItemMeta(meta);
        net.minecraft.server.v1_15_R1.ItemStack is = CraftItemStack.asNMSCopy(item);
        NBTTagCompound tag = is.getOrCreateTag();
        tag.setString("storageBoxType", this.type == null ? "null" : this.type.name());
        tag.setLong("storageBoxAmount", this.amount);
        tag.setBoolean("storageBoxAutoCollect", this.autoCollect);
        tag.setString("randomUUID", id);
        is.setTag(tag);
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
