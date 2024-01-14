package net.minevn.guiapi

import net.minevn.libs.bukkit.color
import net.minevn.libs.bukkit.runSync
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

abstract class ConfiguredUI(
    val viewer: Player?,
    private val configPath: String,
    private val plugin: JavaPlugin,
) : GuiInventory(
    getConfig(configPath, plugin).getInt("rows") * 9,
    getConfig(configPath, plugin).getString("name").color()
) {
    companion object {
        private val configList = mutableMapOf<Plugin, MutableMap<String, YamlConfiguration>>()

        fun getConfig(configPath: String, plugin: JavaPlugin): YamlConfiguration {
            val configsByPlugin = configList.getOrPut(plugin) { mutableMapOf() }
            if (!configsByPlugin.containsKey(configPath)) {
                val configFile = File(plugin.dataFolder, configPath)
                if (!configFile.exists()) {
                    configFile.parentFile.mkdirs()
                    plugin.saveResource(configPath, false)
                }
                configsByPlugin[configPath] = YamlConfiguration.loadConfiguration(configFile)
            }
            return configsByPlugin[configPath]!!
        }

        fun reloadConfig(plugin: JavaPlugin, configPath: String) {
            configList[plugin]?.remove(configPath)
        }

        fun reloadConfigs(plugin: JavaPlugin) = configList.remove(plugin)
    }

    fun getConfig() = getConfig(configPath, plugin)

    fun open() = runSync { viewer?.openInventory(inventory) }
}