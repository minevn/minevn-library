package net.minevn.guiapi;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * A sample gui for a single viewer
 * usage: new TestGui(player)
 */
public class TestGui extends GuiInventory {
	private Player viewer;

	public TestGui(Player viewer) {
		super(9, "This is test GUI");
		buildGui();
		openIventory(viewer);
	}

	public void buildGui() {
		setItem(0, new GuiItemStack(Material.STONE, "Clickable item", "lores goes here!") {
			@Override
			public void onClick(InventoryClickEvent event) {
				lock();
				viewer.sendMessage("action goes here!");
				unlock();
				// locking & unlocking is useful for denying double click or asynchronous actions
			}
		});
	}
}
