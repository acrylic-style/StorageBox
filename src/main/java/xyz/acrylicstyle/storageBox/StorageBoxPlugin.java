package xyz.acrylicstyle.storageBox;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityCombustByBlockEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import util.CollectionList;
import xyz.acrylicstyle.storageBox.utils.StorageBox;
import xyz.acrylicstyle.storageBox.utils.StorageBoxUtils;
import xyz.acrylicstyle.tomeito_api.TomeitoAPI;
import xyz.acrylicstyle.tomeito_api.providers.ConfigProvider;
import xyz.acrylicstyle.tomeito_api.utils.Log;

import java.util.UUID;
import java.util.logging.Logger;

public class StorageBoxPlugin extends JavaPlugin implements Listener {
    public static Logger LOGGER = Logger.getLogger("StorageBox");
    public static ConfigProvider config = new ConfigProvider("./plugins/StorageBox/config.yml");
    public static CollectionList<UUID> bypassingPlayers = new CollectionList<>();

    @Override
    public void onEnable() {
        LOGGER.info("Registering SubCommands");
        TomeitoAPI.getInstance().registerCommands(this.getClassLoader(), "storagebox", "xyz.acrylicstyle.storageBox.commands");
        TomeitoAPI.registerTabCompleter("storagebox", new StorageBoxTabCompleter());
        LOGGER.info("Registering Events");
        Bukkit.getPluginManager().registerEvents(this, this);
        LOGGER.info("Enabled StorageBox");
    }

    @Override
    public void onDisable() {
        LOGGER.info("Saving config");
        config.save();
        LOGGER.info("Saved config");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent e) {
        ItemStack item = e.getPlayer().getInventory().getItemInMainHand();
        StorageBox storageBox = StorageBox.getStorageBox(item);
        if (storageBox == null) {
            item = e.getPlayer().getInventory().getItemInOffHand();
            storageBox = StorageBox.getStorageBox(item);
            if (storageBox == null) return;
        }
        if (storageBox.isEmpty()) {
            e.getPlayer().sendMessage(ChatColor.RED + "Storage Boxが空です。");
            e.setCancelled(true);
            return;
        }
        storageBox.decreaseAmount();
        e.getPlayer().getInventory().setItemInMainHand(StorageBoxUtils.updateStorageBox(item));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerAttemptPickupItem(PlayerAttemptPickupItemEvent e) {
        StorageBox storageBox = StorageBoxUtils.getStorageBoxForType(e.getPlayer().getInventory(), e.getItem().getItemStack().getType());
        if (storageBox == null) return;
        int amount = e.getItem().getItemStack().getAmount();
        Material type = e.getItem().getItemStack().getType();
        e.setCancelled(true);
        e.getItem().getItemStack().setAmount(0);
        e.getItem().remove();
        e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.8F, 1.9F);
        storageBox.setAmount(storageBox.getAmount() + amount);
        ItemStack[] c = e.getPlayer().getInventory().getContents();
        for (int i = 0; i < c.length; i++) {
            StorageBox box = StorageBox.getStorageBox(c[i]);
            if (box == null) continue;
            if ((box.getType() == null ? new Object() : box.getType()).equals(type)) {
                e.getPlayer().getInventory().setItem(i, StorageBoxUtils.updateStorageBox(c[i]));
                break;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemDespawn(ItemDespawnEvent e) {
        StorageBox storageBox = StorageBox.getStorageBox(e.getEntity().getItemStack());
        if (storageBox == null) return;
        Log.info("Despawned storage box (?): " + storageBox.getUniqueId());
        storageBox.delete();
    }

    @EventHandler
    public void onEntityCombustByBlock(EntityCombustByBlockEvent e) {
        if (e.getEntity().getType() != EntityType.DROPPED_ITEM) return;
        StorageBox storageBox = StorageBox.getStorageBox(((Item) e.getEntity()).getItemStack());
        if (storageBox == null) return;
        Log.info("Despawned storage box (Combust): " + storageBox.getUniqueId());
        storageBox.delete();
    }

    @EventHandler
    public void onEntityDamageByBlock(EntityDamageByBlockEvent e) {
        if (e.getEntity().getType() != EntityType.DROPPED_ITEM) return;
        StorageBox storageBox = StorageBox.getStorageBox(((Item) e.getEntity()).getItemStack());
        if (storageBox == null) return;
        Log.info("Despawned storage box (Damage): " + storageBox.getUniqueId());
        storageBox.delete();
    }
}
