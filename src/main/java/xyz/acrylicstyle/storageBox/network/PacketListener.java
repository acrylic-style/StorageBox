package xyz.acrylicstyle.storageBox.network;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.server.v1_15_R1.*;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;

import java.lang.reflect.Field;
import java.util.List;

public class PacketListener extends ChannelDuplexHandler {
    private final EntityPlayer player;

    public PacketListener(EntityPlayer player) {
        this.player = player;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof PacketPlayInBlockPlace && ((PacketPlayInBlockPlace) msg).b() == net.minecraft.server.v1_15_R1.EnumHand.MAIN_HAND) {
            ItemStack stack = player.b(((PacketPlayInBlockPlace) msg).b());
            NBTTagCompound tag = stack.getTag();
            if (tag != null && tag.hasKey("storageBoxType")) {
                // restore item in hand
                //ctx.write(new PacketPlayOutSetSlot(0, player.inventory.itemInHandIndex, player.inventory.getItemInHand()));
            }
        }
        super.channelRead(ctx, msg);
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof PacketPlayOutWindowItems) {
            Field field = PacketPlayOutWindowItems.class.getDeclaredField("b");
            field.setAccessible(true);
            for (ItemStack item : (List<ItemStack>) field.get(msg)) {
                rewriteItem(item);
            }
        } else if (msg instanceof PacketPlayOutEntityEquipment) {
            Field field = PacketPlayOutEntityEquipment.class.getDeclaredField("c");
            field.setAccessible(true);
            rewriteItem((ItemStack) field.get(msg));
        } else if (msg instanceof PacketPlayOutSetSlot) {
            Field field = PacketPlayOutSetSlot.class.getDeclaredField("c");
            field.setAccessible(true);
            rewriteItem((ItemStack) field.get(msg));
        }
        super.write(ctx, msg, promise);
    }

    @SuppressWarnings("deprecation")
    private static void rewriteItem(ItemStack item) {
        if (item == null) return;
        NBTTagCompound tag = item.getTag();
        if (tag == null) return;
        try {
            if (!tag.hasKey("storageBoxType") ||
                    tag.getString("storageBoxType").isEmpty() ||
                    tag.getString("storageBoxType").equals("null")) {
                return;
            }
            if (tag.hasKey("storageBoxTag") && tag.getCompound("storageBoxTag").hasKey("CustomModelData")) {
                tag.setInt("CustomModelData", tag.getCompound("storageBoxTag").getInt("CustomModelData"));
            }
            Material material = Material.valueOf(tag.getString("storageBoxType"));
            if (material == Material.AIR) material = Material.BARRIER;
            item.setItem(CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(material)).getItem());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
