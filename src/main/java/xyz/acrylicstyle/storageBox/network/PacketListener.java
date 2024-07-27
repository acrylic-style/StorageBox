package xyz.acrylicstyle.storageBox.network;

import com.mojang.datafixers.util.Pair;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_20_R2.inventory.CraftItemStack;

public class PacketListener extends ChannelDuplexHandler {
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof ClientboundContainerSetContentPacket packet) {
            for (ItemStack item : packet.getItems()) {
                rewriteItem(item);
            }
            rewriteItem(packet.getCarriedItem());
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
        CompoundTag tag = item.getTag();
        if (tag == null) return;
        try {
            if (!tag.contains("storageBoxType") ||
                    tag.getString("storageBoxType").isEmpty() ||
                    tag.getString("storageBoxType").equals("null")) {
                return;
            }
            if (tag.contains("storageBoxTag") && tag.getCompound("storageBoxTag").contains("CustomModelData")) {
                tag.putInt("CustomModelData", tag.getCompound("storageBoxTag").getInt("CustomModelData"));
            }
            Material material = Material.valueOf(tag.getString("storageBoxType"));
            if (material == Material.AIR) material = Material.BARRIER;
            item.setItem(CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(material)).getItem());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
