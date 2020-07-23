package xyz.acrylicstyle.storageBox;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import util.CollectionList;
import util.ICollectionList;
import xyz.acrylicstyle.paper.block.TileEntity;
import xyz.acrylicstyle.paper.block.TileEntityContainer;
import xyz.acrylicstyle.storageBox.utils.StorageBox;
import xyz.acrylicstyle.storageBox.utils.StorageBoxUtils;
import xyz.acrylicstyle.tomeito_api.TomeitoAPI;
import xyz.acrylicstyle.tomeito_api.providers.ConfigProvider;

import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class StorageBoxPlugin extends JavaPlugin implements Listener {
    public static Logger LOGGER = Logger.getLogger("StorageBox");
    public static ConfigProvider config = null;
    public static CollectionList<UUID> bypassingPlayers = new CollectionList<>();

    @Override
    public void onEnable() {
        LOGGER.info("Loading config");
        config = ConfigProvider.getConfig("./plugins/StorageBox/config.yml");
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

    public void run(Runnable runnable) { Bukkit.getScheduler().runTask(this, runnable); }

    public void runAsync(Runnable runnable) { Bukkit.getScheduler().runTaskAsynchronously(this, runnable); }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent e) {
        ItemStack item = e.getPlayer().getInventory().getItemInMainHand();
        boolean mainHand = true;
        StorageBox storageBox = StorageBox.getStorageBox(item);
        if (storageBox == null) {
            item = e.getPlayer().getInventory().getItemInOffHand();
            storageBox = StorageBox.getStorageBox(item);
            if (storageBox == null) return;
            mainHand = false;
        }
        if (storageBox.isEmpty()) {
            e.getPlayer().sendMessage(ChatColor.RED + "Storage Boxが空です。");
            e.setCancelled(true);
            return;
        }
        boolean finalMainHand1 = mainHand;
        StorageBox finalStorageBox = storageBox;
        runAsync(() -> {
            finalStorageBox.decreaseAmount();
            run(() -> {
                if (finalMainHand1) {
                    e.getPlayer().getInventory().setItemInMainHand(finalStorageBox.getItemStack());
                } else {
                    e.getPlayer().getInventory().setItemInOffHand(finalStorageBox.getItemStack());
                }
            });
            World world = e.getBlockPlaced().getWorld();
            TileEntity te = world.getTileEntity(e.getBlockPlaced().getLocation());
            if (te instanceof TileEntityContainer) {
                ((TileEntityContainer) te).setCustomName(null);
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerAttemptPickupItem(PlayerAttemptPickupItemEvent e) {
        if (!new ItemStack(e.getItem().getItemStack().getType()).isSimilar(e.getItem().getItemStack())) return;
        if (StorageBox.getStorageBox(e.getItem().getItemStack()) != null) return;
        Map.Entry<Integer, StorageBox> storageBox = StorageBoxUtils.getStorageBoxForType(e.getPlayer().getInventory(), e.getItem().getItemStack()).complete();
        if (storageBox == null) return;
        int amount = e.getItem().getItemStack().getAmount();
        e.setCancelled(true);
        e.getItem().getItemStack().setAmount(0);
        e.getItem().remove();
        e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.8F, 1.9F);
        storageBox.getValue().setAmount(storageBox.getValue().getAmount() + amount);
        e.getPlayer().getInventory().setItem(storageBox.getKey(), storageBox.getValue().getItemStack());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockDispense(BlockDispenseEvent e) {
        if (StorageBox.getStorageBox(e.getItem()) != null) e.setCancelled(true);
    }

    @EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent e) {
        if (ICollectionList.asList(e.getInventory().getMatrix()).map(StorageBox::getStorageBox).nonNull().size() != 0) e.getInventory().setResult(null);
        ItemStack item = e.getInventory().getResult();
        if (item == null) return;
        if (StorageBox.getStorageBox(item) == null) return;
        e.getInventory().setResult(StorageBox.getNewStorageBox().getItemStack());
    }

    /* // disabled because it sucks
    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent e) {
        if (e.getInventory().getType() == InventoryType.CHEST) {
            StorageBox storageBox = StorageBox.getStorageBox(e.getPlayer().getInventory().getItemInMainHand());
            if (CollectCommand.fillTo(storageBox, e.getInventory())) return;
            e.getPlayer().getInventory().setItemInMainHand(StorageBoxUtils.updateStorageBox(e.getPlayer().getInventory().getItemInMainHand()));
        }
    }*/

    public static int getEmptySlots(Player p) {
        ItemStack[] cont = p.getInventory().getContents();
        int i = 0;
        for (ItemStack item : cont) if (item == null || item.getType() == Material.AIR) i++;
        return i;
    }
}
