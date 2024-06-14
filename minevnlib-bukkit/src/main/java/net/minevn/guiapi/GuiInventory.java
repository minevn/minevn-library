package net.minevn.guiapi;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

import static net.minevn.libs.bukkit.MineVNLib.parseUIMap;
import static net.minevn.libs.bukkit.MineVNLib.toSlotIds;

public class GuiInventory implements InventoryHolder {
    private final Inventory inv;
    private GuiItemStack[] actions;
    private boolean locked = false;
    private boolean manualHandle = false;

    private Consumer<InventoryClickEvent> onTopClick;
    private Consumer<InventoryClickEvent> onBottomClick;
    private Consumer<InventoryDragEvent> onTopDrag;
    private Consumer<InventoryDragEvent> onBottomDrag;
    private Consumer<InventoryCloseEvent> onClose;

    public GuiInventory(int size, String title) {
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

    public void setItem(String[] slotMap, GuiItemStack item) {
        setItem(toSlotIds(parseUIMap(slotMap)), item);
    }

    public void setItem(List<String> slotMap, GuiItemStack item) {
        setItem(toSlotIds(parseUIMap(slotMap)), item);
    }

    public void removeItem(int slot) {
        if (slot < inv.getSize()) {
            inv.setItem(slot, null);
            actions[slot] = null;
        }
    }

    public void onClick(InventoryClickEvent e) {
        if (!manualHandle) {
            e.setCancelled(true);
        }
        if (!locked) {
            GuiItemStack clickedItem = actions[e.getSlot()];
            if (clickedItem != null) {
                clickedItem.onClick(e);
            }
        }
    }

    public void setOnTopClick(@Nullable Consumer<InventoryClickEvent> onTopClick) {
        this.onTopClick = onTopClick;
    }

    public void setOnBottomClick(@Nullable Consumer<InventoryClickEvent> onBottomClick) {
        this.onBottomClick = onBottomClick;
    }

    public void setOnTopDrag(@Nullable Consumer<InventoryDragEvent> onTopDrag) {
        this.onTopDrag = onTopDrag;
    }

    public void setOnBottomDrag(@Nullable Consumer<InventoryDragEvent> onBottomDrag) {
        this.onBottomDrag = onBottomDrag;
    }

    public void setOnClose(Consumer<InventoryCloseEvent> onClose) {
        this.onClose = onClose;
    }

    public void clear() {
        inv.clear();
        actions = new GuiItemStack[inv.getSize()];
    }

    public void lock() {
        locked = true;
    }

    public void unlock() {
        locked = false;
    }

    public void setManualHandle(boolean manualHandle) {
        this.manualHandle = manualHandle;
    }

    public void openInventory(Player viewer) {
        viewer.openInventory(inv);
    }

    public boolean isViewing(Player viewer) {
        return viewer != null && viewer.getOpenInventory().getTopInventory().getHolder() == this && viewer.isOnline();
    }

    public void onClose(InventoryCloseEvent e) {}

    public Consumer<InventoryClickEvent> getOnTopClick() {
        return onTopClick;
    }

    public Consumer<InventoryClickEvent> getOnBottomClick() {
        return onBottomClick;
    }

    public Consumer<InventoryDragEvent> getOnTopDrag() {
        return onTopDrag;
    }

    public Consumer<InventoryDragEvent> getOnBottomDrag() {
        return onBottomDrag;
    }

    public Consumer<InventoryCloseEvent> getOnClose() {
        return onClose;
    }
}
