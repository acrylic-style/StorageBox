package xyz.acrylicstyle.storageBox.network;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class ChannelUtil {
    public static void inject(Plugin plugin, Player player) {
        try {
            ((CraftPlayer) player)
                    .getHandle()
                    .playerConnection
                    .networkManager
                    .channel
                    .pipeline()
                    .addBefore("packet_handler", "azisaba_storagebox", new PacketListener(((CraftPlayer) player).getHandle()));
        } catch (Exception e) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                try {
                    ((CraftPlayer) player)
                            .getHandle()
                            .playerConnection
                            .networkManager
                            .channel
                            .pipeline()
                            .addBefore("packet_handler", "azisaba_storagebox", new PacketListener(((CraftPlayer) player).getHandle()));
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
                .playerConnection
                .networkManager
                .channel
                .pipeline()
                .remove(PacketListener.class);
    }
}
