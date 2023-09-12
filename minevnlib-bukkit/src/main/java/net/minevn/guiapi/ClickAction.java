package net.minevn.guiapi;

import org.bukkit.event.inventory.InventoryClickEvent;

@FunctionalInterface
public interface ClickAction {
	void onClick(InventoryClickEvent event);
}
