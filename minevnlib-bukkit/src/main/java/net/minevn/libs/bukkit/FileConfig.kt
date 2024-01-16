package net.minevn.libs.bukkit

import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

open class FileConfig(val plugin: JavaPlugin, val name: String) {
    private val file: File = File(plugin.dataFolder, "$name.yml")
    protected lateinit var config: YamlConfiguration private set
    lateinit var baseConfig: YamlConfiguration private set

    init {
        if (!file.exists()) {
            file.parentFile.mkdirs()
            plugin.saveResource("$name.yml", false)
        }
        initYaml()
    }

    private fun initYaml() {
        config = YamlConfiguration.loadConfiguration(file)
        plugin.getResource("$name.yml").use {
            baseConfig = YamlConfiguration.loadConfiguration(InputStreamReader(it, StandardCharsets.UTF_8))
        }
    }

    fun get(key: String): String = (config.getString(key) ?: baseConfig.getString(key, "")).color()

    fun getList(key: String) = (config.getStringList(key)?.takeIf { it.isNotEmpty() } ?: baseConfig.getStringList(key))
        .color()

    open fun reload() {
        initYaml()
    }

    open fun save() {
        config.save(file)
    }
}
