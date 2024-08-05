package xyz.acrylicstyle.storageBox;

import net.milkbowl.vault.economy.Economy;
import net.minecraft.server.v1_15_R1.MojangsonParser;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import xyz.acrylicstyle.storageBox.gui.ShopScreen;
import xyz.acrylicstyle.storageBox.listener.McMMOListener;
import xyz.acrylicstyle.storageBox.listener.MyPetListener;
import xyz.acrylicstyle.storageBox.network.ChannelUtil;
import xyz.acrylicstyle.storageBox.utils.StorageBox;
import xyz.acrylicstyle.storageBox.utils.StorageBoxUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class StorageBoxPlugin extends JavaPlugin implements Listener {
    public static Logger LOGGER;
    public static List<UUID> bypassingPlayers = new ArrayList<>();
    public static Integer customModelData = null;
    public final Map<ItemStack, Long> buyPrices = new ConcurrentHashMap<>();
    public final Map<ItemStack, Long> sellPrices = new ConcurrentHashMap<>();

    @Override
    public void onEnable() {
        LOGGER = getLogger();
        saveDefaultConfig();
        customModelData = (Integer) getConfig().get("custom-model-data");
        loadPrices("buyPrices", buyPrices);
        loadPrices("sellPrices", sellPrices);

        Objects.requireNonNull(Bukkit.getPluginCommand("storagebox")).setTabCompleter(new StorageBoxTabCompleter());
        Objects.requireNonNull(Bukkit.getPluginCommand("storagebox")).setExecutor(new RootCommand());

        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getPluginManager().registerEvents(new ShopScreen.EventListener(), this);

        if (!getConfig().getBoolean("disable-crafting", false)) {
            try {
                ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(this, "storage_box"), StorageBox.getNewStorageBox().getItemStack());
                recipe.shape("DDD", "DCD", "DDD");
                recipe.setIngredient('D', Material.DIAMOND);
                recipe.setIngredient('C', Material.CHEST);
                Bukkit.addRecipe(recipe);
            } catch (RuntimeException ex) {
                // ignore any "dupe recipe" error or something like that
            }
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            ChannelUtil.inject(this, player);
        }

        // delay init
        Bukkit.getScheduler().runTask(this, () -> {
            if (Bukkit.getPluginManager().isPluginEnabled("MyPet")) {
                Bukkit.getPluginManager().registerEvents(new MyPetListener(), this);
            } else {
                getLogger().info("MyPet is not enabled, skipping event registration");
            }
            if (Bukkit.getPluginManager().isPluginEnabled("mcMMO")) {
                Bukkit.getPluginManager().registerEvents(new McMMOListener(), this);
            } else {
                getLogger().info("mcMMO is not enabled, skipping event registration");
            }
        });
    }

    private void loadPrices(@NotNull String path, @NotNull Map<ItemStack, Long> map) {
        ConfigurationSection section = getConfig().getConfigurationSection(path);
        if (section == null) return;
        for (String key : section.getKeys(false)) {
            try {
                int bracketLocation = key.indexOf('{');
                if (bracketLocation == -1) {
                    map.put(new ItemStack(Material.valueOf(key.toUpperCase())), section.getLong(key));
                } else {
                    String material = key.substring(0, bracketLocation);
                    String snbt = key.substring(bracketLocation);
                    NBTTagCompound tag = MojangsonParser.parse(snbt);
                    ItemStack stack = new ItemStack(Material.valueOf(material.toUpperCase()));
                    net.minecraft.server.v1_15_R1.ItemStack nms = CraftItemStack.asNMSCopy(stack);
                    nms.setTag(tag);
                    map.put(CraftItemStack.asBukkitCopy(nms), section.getLong(key));
                }
            } catch (Exception e) {
                getLogger().info("Failed to load " + path + "." + key);
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            ChannelUtil.eject(player);
        }
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        customModelData = (Integer) getConfig().get("custom-model-data");
        loadPrices("buyPrices", buyPrices);
        loadPrices("sellPrices", sellPrices);
    }

    public void run(Runnable runnable) { Bukkit.getScheduler().runTask(this, runnable); }

    public void runAsync(Runnable runnable) { Bukkit.getScheduler().runTaskAsynchronously(this, runnable); }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        ChannelUtil.inject(this, e.getPlayer());
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (e.getPlayer().isOnline()) {
                Inventory inventory = e.getPlayer().getInventory();
                for (int i = 0; i < inventory.getSize(); i++) {
                    ItemStack item = inventory.getItem(i);
                    if (item == null) continue;
                    if (StorageBox.getStorageBox(item) != null) {
                        StorageBox storageBox = StorageBox.getStorageBox(item);
                        if (storageBox != null) {
                            inventory.setItem(i, storageBox.getItemStack());
                        }
                    }
                }
            }
        }, 20 * 5);
    }

    private boolean processing = false;
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        if (processing) return;
        boolean mainHand = e.getHand() == EquipmentSlot.HAND;
        StorageBox storageBox = StorageBox.getStorageBox(mainHand ? e.getPlayer().getInventory().getItemInMainHand() : e.getPlayer().getInventory().getItemInOffHand());
        if (storageBox == null) {
            return;
        }
        if (storageBox.isEmpty()) {
            e.getPlayer().sendMessage(ChatColor.RED + "Storage Boxが空です。");
            e.setCancelled(true);
            return;
        }
        BlockState placedState = e.getBlockPlaced().getState();
        e.setCancelled(true);
        storageBox.decreaseAmount();
        if (mainHand) {
            e.getPlayer().getInventory().setItemInMainHand(storageBox.getItemStack());
        } else {
            e.getPlayer().getInventory().setItemInOffHand(storageBox.getItemStack());
        }
        run(() -> {
            BlockPlaceEvent event = new BlockPlaceEvent(e.getBlockPlaced(), e.getBlockReplacedState(), e.getBlockAgainst(), e.getItemInHand(), e.getPlayer(), e.canBuild(), e.getHand());
            processing = true;
            try {
                Bukkit.getPluginManager().callEvent(event);
            } finally {
                processing = false;
            }
            if (!event.isCancelled()) {
                if (placedState instanceof Container) {
                    ((Container) placedState).setCustomName(storageBox.getComponentItemStackDisplayName());
                }
                placedState.update(true, true);
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerAttemptPickupItem(@NotNull EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player player = (Player) e.getEntity();
        if (StorageBox.getStorageBox(e.getItem().getItemStack()) != null) return;
        Map.Entry<Integer, StorageBox> storageBox = StorageBoxUtils.getStorageBoxForType(player.getInventory(), e.getItem().getItemStack());
        if (storageBox == null) return;
        long amount = e.getItem().getItemStack().getAmount();
        e.setCancelled(true);
        e.getItem().getItemStack().setAmount(0);
        e.getItem().remove();
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.8F, 1.9F);
        storageBox.getValue().setAmount(storageBox.getValue().getAmount() + amount);
        player.getInventory().setItem(storageBox.getKey(), storageBox.getValue().getItemStack());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockDispense(@NotNull BlockDispenseEvent e) {
        if (StorageBox.getStorageBox(e.getItem()) != null) e.setCancelled(true);
    }

    @Contract("null -> new; !null -> param1")
    private static @NotNull ItemStack n(ItemStack item) {
        return item == null ? new ItemStack(Material.AIR) : item;
    }

    @EventHandler
    public void onPrepareItemCraft(@NotNull PrepareItemCraftEvent e) {
        if (getConfig().getBoolean("disable-crafting", false)) {
            return;
        }
        ItemStack[] matrix = e.getInventory().getMatrix();
        if (matrix.length == 9) {
            if (
                    n(matrix[0]).getType() == Material.DIAMOND && n(matrix[1]).getType() == Material.DIAMOND && n(matrix[2]).getType() == Material.DIAMOND
                            && n(matrix[3]).getType() == Material.DIAMOND && n(matrix[4]).getType() == Material.CHEST && n(matrix[5]).getType() == Material.DIAMOND
                            && n(matrix[6]).getType() == Material.DIAMOND && n(matrix[7]).getType() == Material.DIAMOND && n(matrix[8]).getType() == Material.DIAMOND
            ) {
                e.getInventory().setResult(StorageBox.getNewStorageBox().getItemStack());
                return;
            }
        }
        if (Arrays.stream(e.getInventory().getMatrix()).map(StorageBox::getStorageBox).anyMatch(Objects::nonNull)) e.getInventory().setResult(null);
        ItemStack item = e.getInventory().getResult();
        if (item == null) return;
        if (StorageBox.getStorageBox(item) == null) return;
        e.getInventory().setResult(StorageBox.getNewStorageBox().getItemStack());
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent e) {
        if (e.getInventory().getResult() != null && StorageBox.getStorageBox(e.getInventory().getResult()) != null) {
            e.getWhoClicked().sendMessage(ChatColor.GREEN + "アイテムの種類を設定するには、設定したいものをオフハンドに持ったうえで" + ChatColor.YELLOW + "/sb changetype" + ChatColor.GREEN + "を実行してください。");
            e.getWhoClicked().sendMessage(ChatColor.GREEN + "アイテムを取り出すには" + ChatColor.YELLOW + "/sb extract <数>" + ChatColor.GREEN + "を実行してください。");
            e.getWhoClicked().sendMessage(ChatColor.GREEN + "自動収集をオフにするには" + ChatColor.YELLOW + "/sb autocollect" + ChatColor.GREEN + "を実行してください。");
            e.getWhoClicked().sendMessage(ChatColor.GREEN + "その他の使い方などは" + ChatColor.YELLOW + "/sb" + ChatColor.GREEN + "を見てください。");
        }
    }

    @EventHandler
    public void onInventoryClick(@NotNull InventoryClickEvent e) {
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

    public static int getEmptySlots(@NotNull Player p) {
        ItemStack[] cont = p.getInventory().getContents();
        int i = 0;
        for (ItemStack item : cont) if (item == null || item.getType() == Material.AIR) i++;
        if (p.getInventory().getHelmet() == null || p.getInventory().getHelmet().getType() == Material.AIR) i--;
        if (p.getInventory().getChestplate() == null || p.getInventory().getChestplate().getType() == Material.AIR) i--;
        if (p.getInventory().getLeggings() == null || p.getInventory().getLeggings().getType() == Material.AIR) i--;
        if (p.getInventory().getBoots() == null || p.getInventory().getBoots().getType() == Material.AIR) i--;
        if (p.getInventory().getItemInOffHand().getType() == Material.AIR) i--;
        return i;
    }

    public static @NotNull StorageBoxPlugin getInstance() {
        return getPlugin(StorageBoxPlugin.class);
    }

    public static @NotNull Economy getEconomy() {
        RegisteredServiceProvider<Economy> provider = Bukkit.getServicesManager().getRegistration(Economy.class);
        return Objects.requireNonNull(provider).getProvider();
    }
}
