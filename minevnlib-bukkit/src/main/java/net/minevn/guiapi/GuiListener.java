package net.minevn.guiapi;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.java.JavaPlugin;

public class GuiListener implements Listener {
//    private JavaPlugin plugin;

	private GuiListener(JavaPlugin plugin) {
//        this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void onClick(InventoryClickEvent e) {
		Inventory inv = e.getClickedInventory();
		if (inv == null) {
			return;
		}
		InventoryHolder holder = inv.getHolder();
		if (holder != null && holder instanceof GuiInventory) {
			((GuiInventory) holder).onClick(e);
		}
	}

	@EventHandler
	public void onClose(InventoryCloseEvent e) {
		Inventory inv = e.getInventory();
		if (inv == null) {
			return;
		}
		InventoryHolder holder = inv.getHolder();
		if (holder != null && holder instanceof GuiInventory) {
			((GuiInventory) holder).onClose(e);
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
