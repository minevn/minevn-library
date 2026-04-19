package net.minevn.libs.bukkit

import dev.dejvokep.boostedyaml.YamlDocument
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings
import dev.dejvokep.boostedyaml.settings.updater.versioning.BasicVersioning
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

open class FileConfig(val plugin: JavaPlugin, val name: String) {
    private val file: File = File(plugin.dataFolder, "$name.yml")
    lateinit var config: YamlConfiguration private set

    /**
     * Base config is the config file in the jar file
     */
    lateinit var baseConfig: YamlConfiguration private set

    init {
        if (!file.exists()) {
            file.parentFile.mkdirs()
            plugin.saveResource("$name.yml", false)
        }
        initYaml()
    }

    private fun initYaml() {
        plugin.getResource("$name.yml").use {
            checkNotNull(it) { "Could not load default resource $name.yml from plugin jar" }
            YamlDocument.create(
                file,
                it,
                GeneralSettings.DEFAULT,
                LoaderSettings.builder().setAutoUpdate(true).build(),
                DumperSettings.DEFAULT,
                UpdaterSettings.builder()
                    .setVersioning(BasicVersioning("config-version"))
                    .build()
            )
        }

        config = YamlConfiguration.loadConfiguration(file)
        plugin.getResource("$name.yml").use {
            checkNotNull(it) { "Could not load default resource $name.yml from plugin jar" }
            baseConfig = YamlConfiguration.loadConfiguration(InputStreamReader(it, StandardCharsets.UTF_8))
            config.addDefaults(baseConfig)
        }
    }

    open fun get(key: String): String = (config.getString(key) ?: baseConfig.getString(key, ""))!!.color()

    open fun getList(key: String) =
        (config.getStringList(key)?.takeIf { it.isNotEmpty() } ?: baseConfig.getStringList(key))
        .color()

    open fun reload() {
        initYaml()
    }

    open fun save() {
        config.save(file)
    }
}
