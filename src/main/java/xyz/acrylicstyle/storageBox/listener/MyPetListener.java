package xyz.acrylicstyle.storageBox.listener;

import de.Keyle.MyPet.api.event.MyPetPickupItemEvent;
import de.Keyle.MyPet.skill.skills.BackpackImpl;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import xyz.acrylicstyle.storageBox.utils.StorageBox;
import xyz.acrylicstyle.storageBox.utils.StorageBoxUtils;

import java.util.Map;

public class MyPetListener implements Listener {
    @EventHandler
    public void onMyPetPickupItem(MyPetPickupItemEvent e) {
        BackpackImpl backpack = e.getPet().getSkills().get(BackpackImpl.class);
        if (backpack == null || !backpack.isActive()) return;
        Inventory inventory = backpack.getInventory().getBukkitInventory();
        if (StorageBox.getStorageBox(e.getItem().getItemStack()) != null) return;
        Map.Entry<Integer, StorageBox> storageBox = StorageBoxUtils.getStorageBoxForType(inventory, e.getItem().getItemStack());
        if (storageBox == null) return;
        long amount = e.getItem().getItemStack().getAmount();
        e.setCancelled(true);
        e.getItem().getItemStack().setAmount(0);
        e.getItem().remove();
        storageBox.getValue().setAmount(storageBox.getValue().getAmount() + amount);
        inventory.setItem(storageBox.getKey(), storageBox.getValue().getItemStack());
    }
}
