package net.minevn.guiapi

import com.cryptomorin.xseries.XMaterial
import net.minevn.libs.bukkit.color
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

class GuiIcon(
    var iconType: Material,
    var iconData: Short,
    var name: String,
    var lore: List<String>,
    var glow: Boolean,
) {
    fun toItemStack() = ItemStack(iconType, 1, iconData).apply {
        itemMeta = itemMeta.apply {
            setDisplayName(name)
            lore = this@GuiIcon.lore
            if (glow) {
                addEnchant(Enchantment.DURABILITY, 1, true)
            }
            addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES)
        }
    }

    fun toGuiItemStack(action: ClickAction?) = GuiItemStack(toItemStack()).apply { onClick(action) }

    fun toGuiItemStack() = toGuiItemStack(null)

    fun clone() = GuiIcon(iconType, iconData, name, lore, glow)

    companion object {
        @JvmStatic
        fun fromConfig(configSection: ConfigurationSection): GuiIcon {
            val iconType = XMaterial.matchXMaterial(configSection.getString("icon.type", "STONE")!!)
                .orElse(XMaterial.STONE)
                .parseMaterial()!!
            val iconData = configSection.getInt("icon.data", 0).toShort()
            val name = configSection.getString("name", "&f")!!.color()
            val lore = configSection.getStringList("lore").color()
            val glow = configSection.getBoolean("glow")
            return GuiIcon(iconType, iconData, name, lore, glow)
        }

        fun ConfigurationSection.getGuiIcon(path: String) = fromConfig(getConfigurationSection(path)!!)
    }
}