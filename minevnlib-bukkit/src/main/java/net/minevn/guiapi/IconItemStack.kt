package net.minevn.guiapi

import net.minevn.libs.bukkit.setIconData
import org.bukkit.Material

/**
 * Icon GUI lấy model theo custom model data.
 *
 * Khác với [GuiItemStack] vốn nhận durability, lớp này nhận khóa icon-data
 * trong cấu hình rồi gán vào custom model data của item.
 */
open class IconItemStack : GuiItemStack {

    constructor(material: Material, iconData: Short, amount: Int, name: String, lores: List<String>) :
            this(material, iconData, amount, false, name, lores)

    constructor(material: Material, iconData: Short, amount: Int, name: String, vararg lores: String) :
            this(material, iconData, amount, false, name, lores.asList())

    constructor(
        material: Material,
        iconData: Short,
        amount: Int,
        glow: Boolean,
        name: String,
        lores: List<String>,
    ) : super(material, 0.toShort(), amount, glow, name, lores) {
        item.setIconData(iconData)
    }
}
