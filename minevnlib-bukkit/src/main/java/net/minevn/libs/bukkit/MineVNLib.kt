package net.minevn.libs.bukkit

import org.bukkit.plugin.java.JavaPlugin

class MineVNLib : JavaPlugin() {
    companion object {

        /**
         * Parse a map of booleans from a string array, used for GUIAPI. Indicate slots to set the item.
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
         * Convert a map of booleans to an array of slot ids.
         *
         * @param slotMap The map of booleans
         */
        @JvmStatic
        fun toSlotIds(slotMap: Array<Array<Boolean>>) = slotMap
            .mapIndexed { y, line -> line.mapIndexed { x, slot -> if (slot) y * 9 + x else -1 } }
            .flatten()
            .filter { it != -1 }
            .toIntArray()
    }
}