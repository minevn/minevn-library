package net.minevn.guiapi;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.minevn.libs.bukkit.MineVNLib.parseUIMap;
import static net.minevn.libs.bukkit.MineVNLib.toSlotIds;

public class GuiInventory implements InventoryHolder {
    private Inventory inv;
    private GuiItemStack[] actions;
    private int size;
    private boolean locked = false;

    public GuiInventory(int size, String title) {
        this.size = size;
        inv = Bukkit.createInventory(this, size, title);
        actions = new GuiItemStack[size];
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inv;
    }

    public void setItem(int slot, GuiItemStack item) {
        if (slot < inv.getSize()) {
            inv.setItem(slot, item != null ? item.getItem() : null);
            actions[slot] = item;
        }
    }

    public void setItem(int[] slots, GuiItemStack item) {
        for (int i : slots) {
            setItem(i, item);
        }
    }

    public void setItem(GuiItemStack item, int... slots) {
        setItem(slots, item);
    }

    public void setItem(String slotMap, GuiItemStack item) {
        setItem(toSlotIds(parseUIMap(slotMap)), item);
    }

    /**
     * @param slotMap
     * @param item
     */
    public void setItem(String[] slotMap, GuiItemStack item) {
        setItem(toSlotIds(parseUIMap(slotMap)), item);
    }

    public void setItem(List<String> slotMap, GuiItemStack item) {
        setItem(toSlotIds(parseUIMap(slotMap)), item);
    }

    public void onClick(InventoryClickEvent e) {
        e.setCancelled(true);
        if (!locked) {
            GuiItemStack i = actions[e.getSlot()];
            if (i != null) {
                i.onClick(e);
            }
        }
    }

    public void clear() {
        inv.clear();
        actions = new GuiItemStack[size];
    }

    public void lock() {
        locked = true;
    }

    public void unlock() {
        locked = false;
    }

    public void openIventory(Player viewer) {
        viewer.openInventory(inv);
    }

    public boolean isViewing(Player viewer) {
        return viewer != null && viewer.isOnline() && viewer.getOpenInventory().getTopInventory().getHolder() == this;
    }

    public void onClose(InventoryCloseEvent e) {
    }
}
