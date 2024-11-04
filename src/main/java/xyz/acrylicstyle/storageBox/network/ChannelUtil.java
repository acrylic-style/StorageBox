package xyz.acrylicstyle.storageBox.network;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class ChannelUtil {
    public static void inject(Plugin plugin, Player player) {
        try {
            ((CraftPlayer) player)
                    .getHandle()
                    .connection
                    .connection
                    .channel
                    .pipeline()
                    .addBefore("packet_handler", "azisaba_storagebox", new PacketListener());
        } catch (Exception e) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                try {
                    ((CraftPlayer) player)
                            .getHandle()
                            .connection
                            .connection
                            .channel
                            .pipeline()
                            .addBefore("packet_handler", "azisaba_storagebox", new PacketListener());
                } catch (Exception e2) {
                    e2.addSuppressed(e);
                    e2.printStackTrace();
                }
            });
        }
    }

    public static void eject(Player player) {
        ((CraftPlayer) player)
                .getHandle()
                .connection
                .connection
                .channel
                .pipeline()
                .remove(PacketListener.class);
    }
}
