package net.minevn.libs.bukkit

import net.minevn.guiapi.GuiListener
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.plugin.java.JavaPlugin

class MineVNLib : JavaPlugin() {
    companion object {
        lateinit var instance: MineVNLib
            private set

        /**
         * Parse a matrix of booleans from a string array, used for GUIAPI. Indicate slots to set the item.
         *
         * Example:
         * ```
         * #########
         * #########
         * #xxx#xxx#
         * ```
         *
         * Will be converted to:
         * ```
         * [
         *  [false, false, false, false, false, false, false, false, false],
         *  [false, false, false, false, false, false, false, false, false],
         *  [false, true,  true,  true,  false, true,  true,  true,  false]
         * ]
         *  ```
         *
         * @param map The string array
         */
        @JvmStatic
        fun parseUIMap(map: Array<String>) = map
            .map { line -> line.map { char -> char != ' ' && char != '#' }.toTypedArray() }
            .toTypedArray()

        /**
         * Treat each line as a string, then parse the map.
         *
         * @see parseUIMap
         */
        @JvmStatic
        fun parseUIMap(map: String) = parseUIMap(map.split("\n").toTypedArray())

        /**
         * @see parseUIMap
         */
        @JvmStatic
        fun parseUIMap(map: List<String>) = parseUIMap(map.toTypedArray())


        /**
         * Convert a matrix of booleans to an array of slot ids.
         *
         * @param slotMap The matrix of booleans
         */
        @JvmStatic
        fun toSlotIds(slotMap: Array<Array<Boolean>>) = slotMap
            .flatten()
            .mapIndexed { index, isSlot -> if (isSlot) index else -1  }
            .filter { it != -1 }
            .toIntArray()

        fun ConfigurationSection.getGuiFillSlots(path: String) =
            toSlotIds(parseUIMap(getStringList(path)!!.toTypedArray()))
    }

    override fun onEnable() {
        instance = this
        GuiListener.init(this)
        EventListener()
    }
}