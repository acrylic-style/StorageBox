package xyz.acrylicstyle.storageBox;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.acrylicstyle.storageBox.utils.StorageBox;
import xyz.acrylicstyle.storageBox.utils.StorageBoxUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Logger;

public class StorageBoxPlugin extends JavaPlugin implements Listener {
    public static Logger LOGGER = Logger.getLogger("StorageBox");
    public static List<UUID> bypassingPlayers = new ArrayList<>();
    public static FileConfiguration config = null;

    @Override
    public void onEnable() {
        LOGGER.info("Loading config");
        config = getConfig();
        LOGGER.info("Registering SubCommands");
        Objects.requireNonNull(Bukkit.getPluginCommand("storagebox")).setTabCompleter(new StorageBoxTabCompleter());
        Objects.requireNonNull(Bukkit.getPluginCommand("storagebox")).setExecutor(new RootCommand());
        LOGGER.info("Registering Events");
        Bukkit.getPluginManager().registerEvents(this, this);
        LOGGER.info("Enabled StorageBox");
    }

    @Override
    public void onDisable() {
        LOGGER.info("Saving config");
        try {
            getConfig().save(new File("./plugins/StorageBox/config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                BlockState state = e.getBlockPlaced().getState();
                if (state instanceof Chest) {
                    ((Chest) state).setCustomName(null);
                }
            });
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerAttemptPickupItem(EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player player = (Player) e.getEntity();
        if (!new ItemStack(e.getItem().getItemStack().getType()).isSimilar(e.getItem().getItemStack())) return;
        if (StorageBox.getStorageBox(e.getItem().getItemStack()) != null) return;
        Map.Entry<Integer, StorageBox> storageBox = StorageBoxUtils.getStorageBoxForType(player.getInventory(), e.getItem().getItemStack());
        if (storageBox == null) return;
        int amount = e.getItem().getItemStack().getAmount();
        e.setCancelled(true);
        e.getItem().getItemStack().setAmount(0);
        e.getItem().remove();
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.8F, 1.9F);
        storageBox.getValue().setAmount(storageBox.getValue().getAmount() + amount);
        player.getInventory().setItem(storageBox.getKey(), storageBox.getValue().getItemStack());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockDispense(BlockDispenseEvent e) {
        if (StorageBox.getStorageBox(e.getItem()) != null) e.setCancelled(true);
    }

    @EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent e) {
        if (Arrays.stream(e.getInventory().getMatrix()).map(StorageBox::getStorageBox).anyMatch(Objects::nonNull)) e.getInventory().setResult(null);
        ItemStack item = e.getInventory().getResult();
        if (item == null) return;
        if (StorageBox.getStorageBox(item) == null) return;
        e.getInventory().setResult(StorageBox.getNewStorageBox().getItemStack());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getWhoClicked().getGameMode() == GameMode.CREATIVE) return;
        if (e.getClickedInventory() == null) return;
        if (e.getCurrentItem() == null || e.getCurrentItem().getType().isAir()) return;
        if (e.getClickedInventory().getType() == InventoryType.PLAYER || e.getClickedInventory().getType() == InventoryType.WORKBENCH) {
            InventoryType type = e.getInventory().getType();
            if (type == InventoryType.BREWING
                    || type == InventoryType.FURNACE
                    || type == InventoryType.ANVIL
                    || type == InventoryType.GRINDSTONE
                    || type == InventoryType.STONECUTTER) {
                if (StorageBox.getStorageBox(e.getCurrentItem()) == null) return;
                e.setCancelled(true);
            }
        }
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
