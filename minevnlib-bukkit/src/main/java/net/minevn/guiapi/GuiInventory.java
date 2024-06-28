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
    private boolean isDraggingAllowed = false;

    private Consumer<InventoryClickEvent> topClickAction;
    private Consumer<InventoryClickEvent> bottomClickAction;
    private Consumer<InventoryClickEvent> globalClickAction;
    private Consumer<InventoryDragEvent> topDragAction;
    private Consumer<InventoryDragEvent> bottomDragAction;
    private Consumer<InventoryCloseEvent> closeAction;

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
        setItem(slot, null);
    }

    public void onClick(InventoryClickEvent e) {
        if (!isDraggingAllowed) {
            e.setCancelled(true);
        }
        if (!locked) {
            GuiItemStack clickedItem = actions[e.getSlot()];
            if (clickedItem != null) {
                clickedItem.onClick(e);
            }
        }
    }

    public void setTopClickAction(@Nullable Consumer<InventoryClickEvent> topClickAction) { this.topClickAction = topClickAction; }

    public void setBottomClickAction(@Nullable Consumer<InventoryClickEvent> bottomClickAction) { this.bottomClickAction = bottomClickAction; }

    public void setGlobalClickAction(@Nullable Consumer<InventoryClickEvent> globalClickAction) {this.globalClickAction = globalClickAction; }

    public void setTopDragAction(@Nullable Consumer<InventoryDragEvent> topDragAction) { this.topDragAction = topDragAction; }

    public void setBottomDragAction(@Nullable Consumer<InventoryDragEvent> bottomDragAction) { this.bottomDragAction = bottomDragAction; }

    public void setCloseAction(Consumer<InventoryCloseEvent> closeAction) { this.closeAction = closeAction; }

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

    public void setDraggingAllowed(boolean isDraggingAllowed) { this.isDraggingAllowed = isDraggingAllowed; }

    public void openIventory(Player viewer) {
        viewer.openInventory(inv);
    }

    public boolean isViewing(Player viewer) {
        return viewer != null && viewer.isOnline() && viewer.getOpenInventory().getTopInventory().getHolder() == this;
    }

    public void onClose(InventoryCloseEvent e) {}

    public Consumer<InventoryClickEvent> getTopClickAction() { return topClickAction; }

    public Consumer<InventoryClickEvent> getBottomClickAction() { return bottomClickAction; }

    public Consumer<InventoryClickEvent> getGlobalClickAction() { return globalClickAction; }

    public Consumer<InventoryDragEvent> getTopDragAction() {
        return topDragAction;
    }

    public Consumer<InventoryDragEvent> getBottomDragAction() {
        return bottomDragAction;
    }

    public Consumer<InventoryCloseEvent> getCloseAction() {
        return closeAction;
    }
}
