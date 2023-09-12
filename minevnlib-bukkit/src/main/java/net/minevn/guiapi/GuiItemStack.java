package net.minevn.guiapi;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.List;

public class GuiItemStack {
	private ItemStack item;
	private ClickAction action = null;

	public GuiItemStack(Material material, int amount) {
		item = new ItemStack(material, amount);
	}

	public GuiItemStack(Material material) {
		this(material, "§f");
	}

	public GuiItemStack(Material material, String name, String... lores) {
		this(material, name, Arrays.asList(lores));
	}

	public GuiItemStack(Material material, int amount, String name, String... lores) {
		this(material, amount, name, Arrays.asList(lores));
	}

	public GuiItemStack(Player skullOwner, String name, String... lores) {
		this(skullOwner, 1, false, name, Arrays.asList(lores));
	}

	public GuiItemStack(Material material, String name, List<String> lores) {
		this(material, 1, name, lores);
	}

	public GuiItemStack(Material material, int amount, String name, List<String> lores) {
		this(material, amount, false, name, lores);
	}

	public GuiItemStack(Player skullOwner, int amount, boolean glow, String name, List<String> lores) {
		this(XMaterial.PLAYER_HEAD.parseMaterial(), XMaterial.PLAYER_HEAD.getData(), amount, glow, name, lores);
		SkullMeta sm = (SkullMeta) item.getItemMeta();
//		sm.setOwningPlayer(skullOwner);
		sm.setPlayerProfile(skullOwner.getPlayerProfile());
		item.setItemMeta(sm);
	}

	public GuiItemStack(Material material, int amount, boolean glow, String name, List<String> lores) {
		this(material, (byte) 0, amount, glow, name, lores);
	}

	public GuiItemStack(Material material, byte data, int amount, boolean glow, String name, List<String> lores) {
		item = new ItemStack(material, amount, data);
		ItemMeta im = item.getItemMeta();
		im.setDisplayName(name);
		if (lores != null && lores.size() > 0)
			im.setLore(lores);
		if (glow) {
			im.addEnchant(Enchantment.DURABILITY, 1, true);
		}
		im.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
		item.setItemMeta(im);
	}

	public GuiItemStack(Material material, short data, int amount, String name, List<String> lores) {
		this(material, data, amount, false, name, lores);
	}

	public GuiItemStack(Material material, short data, int amount, boolean glow, String name, List<String> lores) {
		item = new ItemStack(material, amount, data);
		ItemMeta im = item.getItemMeta();
		im.setDisplayName(name);
		if (lores != null && lores.size() > 0) {
			im.setLore(lores);
		}
		if (glow) {
			im.addEnchant(Enchantment.DURABILITY, 1, true);
		}
		im.setUnbreakable(true);
		im.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
		item.setItemMeta(im);
	}

	public GuiItemStack(Material material, short data, int amount, String name, String... lores) {
		this(material, data, amount, name, Arrays.asList(lores));
	}

	public GuiItemStack(Material material, short data, String name, String... lores) {
		this(material, data, 1, name, lores);
	}

	public GuiItemStack(Material material, short data, int amount, boolean glow, String name, String... lores) {
		this(material, data, amount, glow, name, Arrays.asList(lores));
	}

	public GuiItemStack(ItemStack item, String name, String... lores) {
		this.item = item;
		ItemMeta im = item.getItemMeta();
		im.setDisplayName(name);
		if (lores != null && lores.length > 0)
			im.setLore(Arrays.asList(lores));
		im.setUnbreakable(true);
		im.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
		item.setItemMeta(im);
	}

	public GuiItemStack(ItemStack item) {
		this.item = item;
	}

	public void onClick(InventoryClickEvent event) {
		if (action != null) action.onClick(event);
	}

	public GuiItemStack onClick(ClickAction action) {
		this.action = action;
		return this;
	}

	public ItemStack getItem() {
		return item;
	}
}
