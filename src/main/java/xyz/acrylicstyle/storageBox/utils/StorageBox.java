package xyz.acrylicstyle.storageBox.utils;

import net.minecraft.server.v1_15_R1.NBTTagCompound;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.acrylicstyle.storageBox.StorageBoxPlugin;

import java.util.*;
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
    private @Nullable Material type;
    private long amount;
    private @Nullable NBTTagCompound tag;
    private final @Nullable UUID randomUUID;

    public StorageBox(@Nullable Material type, long amount) {
        this(type, amount, true, null, null);
    }

    public StorageBox(@Nullable Material type, long amount, boolean autoCollect, @Nullable UUID randomUUID) {
        this(type, amount, autoCollect, null, randomUUID);
    }

    public StorageBox(@Nullable Material type, long amount, boolean autoCollect, @Nullable NBTTagCompound tag, @Nullable UUID randomUUID) {
        this.type = type;
        this.amount = amount;
        this.autoCollect = autoCollect;
        this.tag = tag;
        this.randomUUID = randomUUID;
    }

    public static @Nullable StorageBox getStorageBox(@NotNull ItemStack itemStack) {
        try {
            NBTTagCompound tag = CraftItemStack.asNMSCopy(itemStack).getOrCreateTag();
            if (!tag.hasKey("storageBoxType")) {
                return null;
            }
            String s = tag.getString("storageBoxType");
            Material type = Material.valueOf(s.isEmpty() || s.equals("null") ? "AIR" : s.toUpperCase());
            long amount = tag.getLong("storageBoxAmount");
            boolean autoCollect = tag.getBoolean("storageBoxAutoCollect");
            NBTTagCompound storageBoxTag = tag.getCompound("storageBoxTag");
            if (storageBoxTag.hasKey("storageBoxAmount")) {
                throw new IllegalArgumentException("StorageBox cannot contain StorageBox");
            }
            if (storageBoxTag.isEmpty()) storageBoxTag = null;
            UUID randomUUID = UUID.fromString(tag.getString("randomUUID"));
            return new StorageBox(type, amount, autoCollect, storageBoxTag, randomUUID);
        } catch (RuntimeException e) {
            return null;
        }
    }

    public static StorageBox getNewStorageBox() {
        return getNewStorageBox(null);
    }

    public static StorageBox getNewStorageBox(Material type) {
        return getNewStorageBox(type, 0);
    }

    public static StorageBox getNewStorageBox(Material type, long amount) {
        return new StorageBox(type, amount);
    }

    public static @NotNull StorageBox wrapWithStorageBox(@NotNull ItemStack stack) {
        NBTTagCompound tag = CraftItemStack.asNMSCopy(stack).getTag();
        if (tag != null && tag.isEmpty()) tag = null;
        return new StorageBox(stack.getType(), stack.getAmount(), true, tag, null);
    }

    /**
     * Returns the containing item. Amount is always 1.
     * @return the item
     */
    public @Nullable ItemStack getComponentItemStack() {
        ItemStack stack = new ItemStack(type == null ? Material.AIR : type);
        if (type == null || type.isAir() || tag == null) return stack;
        net.minecraft.server.v1_15_R1.ItemStack nms = CraftItemStack.asNMSCopy(stack);
        nms.setTag(tag);
        return CraftItemStack.asBukkitCopy(nms);
    }

    public @NotNull String getComponentItemStackName() {
        if (type == null || type.isAir()) return "空";
        ItemStack stack = getComponentItemStack();
        if (Objects.requireNonNull(stack).hasItemMeta() && Objects.requireNonNull(stack.getItemMeta()).hasDisplayName()) {
            return Objects.requireNonNull(stack.getItemMeta()).getDisplayName();
        }
        String name = type.name().replaceAll("_", " ").toLowerCase();
        return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
    }

    public @Nullable String getComponentItemStackDisplayName() {
        if (type == null || type.isAir()) return null;
        ItemStack stack = getComponentItemStack();
        if (Objects.requireNonNull(stack).hasItemMeta() && Objects.requireNonNull(stack.getItemMeta()).hasDisplayName()) {
            return Objects.requireNonNull(stack.getItemMeta()).getDisplayName();
        }
        return null;
    }

    public boolean isComponentItemStackSimilar(@Nullable ItemStack stack) {
        if (stack == null) return false;
        return stack.isSimilar(getComponentItemStack());
    }

    public @NotNull ItemStack getItemStack() {
        Material itemType = getType() == null ? Material.BARRIER : getType();
        if (!itemType.isBlock() && !opaqueExempt.contains(itemType)) {
            itemType = Material.STICK;
        }
        String id = randomUUID != null ? randomUUID.toString() : UUID.randomUUID().toString();
        ItemStack item = new ItemStack(itemType);
        net.minecraft.server.v1_15_R1.ItemStack is = CraftItemStack.asNMSCopy(item);
        NBTTagCompound tag = is.getOrCreateTag();
        if (this.tag != null) {
            tag.a(this.tag); // merge (for BlockState)
            tag.set("storageBoxTag", this.tag.clone());
            tag.remove("MYTHIC_TYPE");
            tag.remove("AttributeModifiers");
            tag.remove("display");
            tag.remove("Enchantments");
            tag.remove("CustomModelData");
            tag.remove("LifeItemId");
            tag.remove("backup");
        }
        tag.setString("storageBoxType", this.type == null ? "null" : this.type.name());
        tag.setLong("storageBoxAmount", this.amount);
        tag.setBoolean("storageBoxAutoCollect", this.autoCollect);
        tag.setString("randomUUID", id);
        is.setTag(tag);
        item = CraftItemStack.asBukkitCopy(is);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            throw new RuntimeException("ItemMeta is null");
        }
        meta.setDisplayName("§dStorage Box §e[§f" + getComponentItemStackName() + "§r§e] §7<" + this.amount + ">");
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "数: " + amount,
                ChatColor.GRAY + "自動回収: " + autoCollect,
                ChatColor.GRAY + "NBTタグ: " + (getTag() != null),
                ChatColor.GRAY + "不透明: " + (itemType == Material.STICK) + " (" + itemType + ")",
                ChatColor.GRAY + "ID: " + id
        ));
        if (type == null || type.isAir()) {
            meta.setCustomModelData(StorageBoxPlugin.customModelData);
        }
        if (amount > 0) {
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        item.setItemMeta(meta);
        return item;
    }

    public void setAmount(long amount) {
        this.amount = amount;
        if (amount <= 0 && type == Material.EMERALD_BLOCK) {
            type = null;
            tag = null;
        }
    }

    public void increaseAmount() {
        setAmount(amount + 1);
    }

    public void decreaseAmount() {
        setAmount(amount - 1);
    }

    /**
     * Get material of this storage box.
     * @return Null if undefined, material otherwise.
     */
    public @Nullable Material getType() {
        return type == null || type.isAir() ? null : type;
    }

    /**
     * Set material of this storage box.
     * @param type Null if undefined, material otherwise.
     */
    public void setType(@Nullable Material type) {
        this.type = type;
    }

    public long getAmount() {
        return amount;
    }

    public @Nullable NBTTagCompound getTag() {
        return tag;
    }

    public void setTag(@Nullable NBTTagCompound tag) {
        if (tag != null && tag.hasKey("storageBoxAmount")) {
            throw new IllegalArgumentException("StorageBox cannot contain StorageBox");
        }
        this.tag = tag;
    }

    public boolean isEmpty() {
        return amount <= 0;
    }

    public boolean isAutoCollect() {
        return autoCollect;
    }

    public void setAutoCollect(boolean autoCollect) {
        this.autoCollect = autoCollect;
    }

    public void importComponent(@NotNull ItemStack stack) {
        NBTTagCompound tag = CraftItemStack.asNMSCopy(stack).getTag();
        if (tag != null && tag.isEmpty()) tag = null;
        this.setTag(tag);
        this.type = stack.getType();
        this.amount = stack.getAmount();
    }
}
