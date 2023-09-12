package net.minevn.guiapi;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public class TestPagination extends GuiInventory {
	private static final int pagesize = 45;
	private Player viewer;
	int page = 0;
	private List<Object> items;

	public TestPagination(Player viewer, List<Object> items) {
		super(54, "test");
		this.viewer = viewer;
		this.items = items;
	}

	private void build() {
		// trang tri
		for (int slot = 45; slot <= 53; slot++) {
			setItem(slot, new GuiItemStack(Material.STAINED_GLASS_PANE, (short) 15, "§f"));
		}

		// close
		setItem(49, new GuiItemStack(Material.BARRIER, "Close")
				.onClick(e -> viewer.closeInventory()));

		// pagination
		if (page > 0) {
			setItem(45, new GuiItemStack(Material.STAINED_GLASS_PANE, (short) 13, "§f").onClick(e -> {
				page--;
				build();
			}));
		}
		if (pagesize * (page + 1) < items.size()) {
			setItem(53, new GuiItemStack(Material.STAINED_GLASS_PANE, (short) 13, "§f").onClick(e -> {
				page--;
				build();
			}));
		}

		// items
		int i = 0;
		for (int n = pagesize * page; n < pagesize * (page + 1) && n < items.size(); n++) {
			Object item = items.get(n);
			// actions
		}
	}
}
