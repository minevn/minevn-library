package net.minevn.guiapi

import net.minevn.libs.bukkit.color
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack

class GuiIcon(
    var iconType: Material,
    var iconData: Short,
    var name: String,
    var lores: List<String>,
) {
    fun toItemStack() = ItemStack(iconType, 1, iconData).apply {
        val meta = itemMeta
        meta.setDisplayName(name)
        meta.lore = lore
        itemMeta = meta
    }

    fun toGuiItemStack(action: ClickAction?) = GuiItemStack(toItemStack()).apply { onClick(action) }

    fun toGuiItemStack() = toGuiItemStack(null)

    fun clone() = GuiIcon(iconType, iconData, name, lores)

    companion object {
        @JvmStatic
        fun fromConfig(configSection: ConfigurationSection) : GuiIcon {
            val iconType = XMaterial.matchXMaterial(configSection.getString("icon.type", "STONE"))
                .orElse(XMaterial.STONE)
                .parseMaterial()!!
            val iconData = configSection.getInt("icon.data", 0).toShort()
            val name = configSection.getString("name", "&f").color()
            val lore = configSection.getStringList("lores").color()
            return GuiIcon(iconType, iconData, name, lore)
        }

        fun ConfigurationSection.getGuiIcon(path: String) = fromConfig(getConfigurationSection(path)!!)
    }
}