package xyz.acrylicstyle.storageBox.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import xyz.acrylicstyle.storageBox.StorageBoxPlugin;
import xyz.acrylicstyle.storageBox.utils.ItemUtil;
import xyz.acrylicstyle.storageBox.utils.StorageBox;

import java.util.*;

public final class ShopScreen implements InventoryHolder {
    private final Player player;
    private final List<ShopEntry> shopEntries = new ArrayList<>();
    private final Inventory inventory = Bukkit.createInventory(this, 54, "StorageBox Shop");
    private List<ShopEntry> entriesInCurrentPage = new ArrayList<>();
    private int pageIndex = 0;

    public ShopScreen(@NotNull Player player, @NotNull Map<ItemStack, Long> buyPrices) {
        this.player = player;
        buyPrices.forEach((item, price) -> {
            if (price == 0) return;
            long storageBoxBuyPrice = StorageBoxPlugin.getInstance().getConfig().getLong("storageBoxBuyPrice");
            shopEntries.add(new ShopEntry(StorageBox.wrapWithStorageBox(item), price + storageBoxBuyPrice));
        });
        resetItems();
    }

    private void resetItems() {
        inventory.clear();
        int fromIndex = pageIndex * 45;
        int toIndex = Math.min((pageIndex + 1) * 45, shopEntries.size());
        entriesInCurrentPage = shopEntries.subList(fromIndex, toIndex);
        for (int i = 0; i < entriesInCurrentPage.size(); i++) {
            ShopEntry entry = entriesInCurrentPage.get(i);
            ItemStack item = entry.storageBox.getItemStack();
            ItemMeta meta = item.getItemMeta();
            assert meta != null;
            List<String> lore = new ArrayList<>(Objects.requireNonNull(meta.getLore()));
            lore.add("");
            if (StorageBoxPlugin.getEconomy().has(player, entry.price)) {
                lore.add("§e値段: §a$" + entry.price);
            } else {
                lore.add("§e値段: §c$" + entry.price);
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
            inventory.setItem(i, item);
        }
        inventory.setItem(45, ItemUtil.createItem(Material.ARROW, "§a← 前のページ", Collections.emptyList()));
        inventory.setItem(49, ItemUtil.createItem(Material.BARRIER, "§c閉じる", Collections.emptyList()));
        inventory.setItem(53, ItemUtil.createItem(Material.ARROW, "§a次のページ →", Collections.emptyList()));
    }

    public int getMaxPageIndex() {
        return (int) Math.floor((double) shopEntries.size() / 45);
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return inventory;
    }

    private static class ShopEntry {
        public final StorageBox storageBox;
        public final long price;

        public ShopEntry(@NotNull StorageBox storageBox, long price) {
            this.storageBox = storageBox;
            this.price = price;
        }
    }

    public static class EventListener implements Listener {
        @EventHandler
        public void onInventoryDrag(InventoryDragEvent e) {
            if (e.getInventory().getHolder() instanceof ShopScreen) {
                e.setCancelled(true);
            }
        }

        @EventHandler
        public void onInventoryClick(InventoryClickEvent e) {
            if (!(e.getInventory().getHolder() instanceof ShopScreen)) {
                return;
            }
            e.setCancelled(true);
            if (e.getClickedInventory() == null || !(e.getClickedInventory().getHolder() instanceof ShopScreen)) {
                return;
            }
            ShopScreen screen = (ShopScreen) e.getInventory().getHolder();
            if (e.getSlot() < 45) {
                if (e.getSlot() >= screen.entriesInCurrentPage.size()) return;
                ShopEntry entry = screen.entriesInCurrentPage.get(e.getSlot());
                if (StorageBoxPlugin.getEconomy().withdrawPlayer(screen.player, entry.price).transactionSuccess()) {
                    screen.player.getInventory().addItem(entry.storageBox.getItemStack());
                    screen.player.sendMessage(ChatColor.GREEN + "アイテムを購入し、$" + entry.price + "が口座から引き落とされました。");
                }
                return;
            }
            if (e.getSlot() == 45 && screen.pageIndex > 0) {
                screen.pageIndex--;
                screen.resetItems();
            }
            if (e.getSlot() == 49) {
                e.getWhoClicked().closeInventory();
            }
            if (e.getSlot() == 53 && screen.pageIndex < screen.getMaxPageIndex()) {
                screen.pageIndex++;
                screen.resetItems();
            }
        }
    }
}
