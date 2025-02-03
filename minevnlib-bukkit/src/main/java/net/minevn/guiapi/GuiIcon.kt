package net.minevn.guiapi

import com.cryptomorin.xseries.XMaterial
import net.minevn.libs.bukkit.color
import net.minevn.libs.bukkit.hideAll
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack

class GuiIcon(
    var iconItem: ItemStack,
    var iconData: Short,
    var name: String,
    var lore: List<String>,
    var glow: Boolean,
) {
    fun toItemStack() = iconItem.clone().apply {
        if (iconData > 0) {
            durability = iconData
        }
        itemMeta = itemMeta.apply {
            setDisplayName(name)
            lore = this@GuiIcon.lore
            if (glow) {
                addEnchant(Enchantment.DURABILITY, 1, true)
            }
            hideAll()
        }
    }

    fun toGuiItemStack(action: ClickAction?) = GuiItemStack(toItemStack()).apply { onClick(action) }

    fun toGuiItemStack() = toGuiItemStack(null)

    fun clone() = GuiIcon(iconItem, iconData, name, lore, glow)

    companion object {
        @JvmStatic
        fun fromConfig(configSection: ConfigurationSection): GuiIcon {
            val iconItem = XMaterial.matchXMaterial(configSection.getString("icon.type", "STONE")!!)
                .orElse(XMaterial.STONE)
                .parseItem()!!
            val iconData = configSection.getInt("icon.data", 0).toShort()
            val name = configSection.getString("name", "&f")!!.color()
            val lore = configSection.getStringList("lore").color()
            val glow = configSection.getBoolean("glow")
            return GuiIcon(iconItem, iconData, name, lore, glow)
        }

        fun ConfigurationSection.getGuiIcon(path: String) = fromConfig(getConfigurationSection(path)!!)
    }
}
