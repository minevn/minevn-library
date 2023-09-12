package net.minevn.guiapi;

import org.bukkit.plugin.java.JavaPlugin;

public class GuiAPI extends JavaPlugin {
	@Override
	public void onEnable() {
		GuiListener.init(this);
	}
}
