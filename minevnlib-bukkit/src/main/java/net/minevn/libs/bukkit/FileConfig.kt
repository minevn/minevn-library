package net.minevn.libs.bukkit

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
        config = YamlConfiguration.loadConfiguration(file)
        plugin.getResource("$name.yml").use {
            baseConfig = YamlConfiguration.loadConfiguration(InputStreamReader(it, StandardCharsets.UTF_8))
            config.addDefaults(baseConfig)
        }
    }

    open fun get(key: String): String = (config.getString(key) ?: baseConfig.getString(key, ""))!!.color()

    open fun getList(key: String) =
        (config.getStringList(key)?.takeIf { it.isNotEmpty() } ?: baseConfig.getStringList(key))
        .color()

    /**
     * Giống [getList] nhưng giữ nguyên list rỗng thay vì fallback về file mẫu trong jar,
     * dùng cho các message muốn tắt được bằng `key: []`.
     *
     * Key không có trong file thì vẫn lấy giá trị trong file mẫu (config đã gắn default từ file mẫu).
     */
    open fun getOptionalList(key: String) = config.getStringList(key).color()

    /**
     * Lấy giá trị admin thực sự đặt trong file.
     *
     * Không dùng `getString` trực tiếp vì config đã gắn default từ file mẫu trong jar, key bị xóa khỏi file
     * vẫn trả về giá trị mẫu (`abc`, `password`...).
     *
     * @return giá trị của key, hoặc null nếu key không có trong file, rỗng, hoặc trùng giá trị trong file mẫu.
     */
    fun getCustomString(key: String): String? {
        if (!config.isSet(key)) {
            return null
        }
        val value = config.getString(key)?.takeIf { it.isNotBlank() } ?: return null
        if (value == baseConfig.getString(key)) {
            return null
        }
        return value
    }

    open fun reload() {
        initYaml()
    }

    open fun save() {
        config.save(file)
    }
}
