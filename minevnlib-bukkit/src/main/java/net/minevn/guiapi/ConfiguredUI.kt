package net.minevn.guiapi

import net.minevn.libs.bukkit.color
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

abstract class ConfiguredUI(
    val viewer: Player,
    private val configPath: String,
    private val plugin: JavaPlugin,
) : GuiInventory(
    getConfig(configPath, plugin).getInt("rows"),
    getConfig(configPath, plugin).getString("title").color()
) {
    companion object {
        private val configList = mutableMapOf<String, YamlConfiguration>()

        fun getConfig(configPath: String, plugin: JavaPlugin) : YamlConfiguration {
            if (!configList.containsKey(configPath)) {
                configList[configPath] = YamlConfiguration.loadConfiguration(File(plugin.dataFolder, configPath))
            }
            return configList[configPath]!!
        }

        fun reloadConfig(configPath: String) {
            configList.remove(configPath)
        }

        fun reloadConfigs() = configList.clear()
    }

    fun getConfig() = getConfig(configPath, plugin)
}