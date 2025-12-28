package xyz.acrylicstyle.storageBox.network;

import com.mojang.datafixers.util.Pair;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftItemStack;

public class PacketListener extends ChannelDuplexHandler {
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof ClientboundContainerSetContentPacket packet) {
            for (ItemStack item : packet.items()) {
                rewriteItem(item);
            }
            rewriteItem(packet.carriedItem());
        } else if (msg instanceof ClientboundSetEquipmentPacket packet) {
            for (Pair<EquipmentSlot, ItemStack> pair : packet.getSlots()) {
                rewriteItem(pair.getSecond());
            }
        }
        super.write(ctx, msg, promise);
    }

    @SuppressWarnings("deprecation")
    private static void rewriteItem(ItemStack item) {
        if (item == null) return;
        CustomData customData = item.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return;
        CompoundTag tag = customData.copyTag();
        try {
            if (!tag.contains("storageBoxType") ||
                    tag.getString("storageBoxType").isEmpty() ||
                    tag.getString("storageBoxType").orElse("null").isEmpty()) {
                return;
            }
            if (tag.contains("storageBoxTag") && tag.getCompound("storageBoxTag").orElseGet(CompoundTag::new).contains("CustomModelData")) {
                tag.putInt("CustomModelData", tag.getCompound("storageBoxTag").orElseGet(CompoundTag::new).getInt("CustomModelData").orElse(0));
            }
            Material material = Material.valueOf(tag.getString("storageBoxType").orElse("AIR").toUpperCase());
            if (material == Material.AIR) material = Material.BARRIER;
            item.setItem(CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(material)).getItem());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
