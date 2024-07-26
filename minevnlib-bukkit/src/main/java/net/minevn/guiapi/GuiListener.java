package net.minevn.guiapi;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.Set;

public class GuiListener implements Listener {
//    private JavaPlugin plugin;

    private GuiListener(JavaPlugin plugin) {
//        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Inventory inv = e.getInventory();
        if (inv == null) {
            return;
        }
        InventoryHolder holder = inv.getHolder();
        if (holder == null) {
            return;
        }
        if (e.getSlotType() == InventoryType.SlotType.OUTSIDE) {
            return;
        }
        if (holder instanceof GuiInventory) {
            GuiInventory guiInventory = (GuiInventory) holder;
            if (guiInventory.getGlobalClickAction() != null) {
                guiInventory.getGlobalClickAction().accept(e);
            }
            if (e.getClickedInventory() == e.getView().getTopInventory()) {
                guiInventory.onClick(e);
                if (guiInventory.getTopClickAction() != null) {
                    guiInventory.getTopClickAction().accept(e);
                }
            } else {
                if (guiInventory.getBottomClickAction() != null) {
                    guiInventory.getBottomClickAction().accept(e);
                }
            }
        }
    }

    /**
     * Handle inventory drag event (Credit to @stefvanschie)
     */
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        Inventory inv = e.getInventory();
        if (inv == null) {
            return;
        }
        InventoryHolder holder = inv.getHolder();
        if (holder == null) {
            return;
        }
        if (holder instanceof GuiInventory == false) {
            return;
        }
        GuiInventory guiInventory = (GuiInventory) holder;
        InventoryView view = e.getView();
        Set<Integer> inventorySlots = e.getRawSlots();
        boolean isTopClick = false, isBottomClick = false;
        for (int inventorySlot : inventorySlots) {
            Inventory inventory = view.getInventory(inventorySlot);

            if (view.getTopInventory().equals(inventory)) {
                isTopClick = true;
            } else if (view.getBottomInventory().equals(inventory)) {
                isBottomClick = true;
            }

            if (isTopClick && isBottomClick) {
                break;
            }
        }
        if (isTopClick && guiInventory.getTopDragAction() != null) {
            guiInventory.getTopDragAction().accept(e);
        }
        if (isBottomClick && guiInventory.getBottomDragAction() != null) {
            guiInventory.getBottomDragAction().accept(e);
        }
        // If the drag event occurred in only one slot
        if (inventorySlots.size() == 1) {
            int index = inventorySlots.toArray(new Integer[0])[0];
            InventoryType.SlotType slotType = view.getSlotType(index);
            boolean even = e.getType() == DragType.EVEN;
            ClickType clickType = even ? ClickType.LEFT : ClickType.RIGHT;
            InventoryAction inventoryAction = even ? InventoryAction.PLACE_SOME : InventoryAction.PLACE_ONE;
            ItemStack previousViewCursor = view.getCursor();
            // Overwrite getCursor in inventory click event to mimic real event fired by Bukkit.
            view.setCursor(e.getOldCursor());
            //this is a fake click event, firing this may cause other plugins to function incorrectly, so keep it local
            InventoryClickEvent inventoryClickEvent = new InventoryClickEvent(view, slotType, index, clickType,
                    inventoryAction);
            guiInventory.onClick(inventoryClickEvent);
            // Restore previous cursor only if someone has not changed it manually in onInventoryClick.
            if (Objects.equals(view.getCursor(), e.getOldCursor())) {
                view.setCursor(previousViewCursor);
            }
            e.setCancelled(inventoryClickEvent.isCancelled());
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        Inventory inv = e.getInventory();
        if (inv == null) {
            return;
        }
        InventoryHolder holder = inv.getHolder();
        if (holder == null) {
            return;
        }
        if (holder instanceof GuiInventory) {
            GuiInventory guiInventory = (GuiInventory) holder;
            guiInventory.onClose(e);
            if (guiInventory.getCloseAction() != null) {
                guiInventory.getCloseAction().accept(e);
            }
        }
    }

    // static
    private static GuiListener _instance;

    public static GuiListener getInstance() {
        return _instance;
    }

    public static void init(JavaPlugin plugin) {
        if (_instance != null) {
            throw new IllegalArgumentException("GUI API already initialized.");
        }
        _instance = new GuiListener(plugin);
    }
}
