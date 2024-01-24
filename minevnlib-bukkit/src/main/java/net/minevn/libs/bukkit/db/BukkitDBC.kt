package net.minevn.libs.bukkit.db

import net.minevn.libs.db.connection.types.H2DBC
import net.minevn.libs.db.connection.types.MariaDBC
import net.minevn.libs.db.connection.types.MyDBC
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Level

class BukkitDBC {
    companion object {
        fun init(plugin: JavaPlugin, dbType: String, config: YamlConfiguration) = run {
            val logger: (String) -> Unit = plugin.logger::info
            val exceptionLogger: (Level, String, Throwable) -> Unit = plugin.logger::log
            val dbConfig = if (dbType == "mariadb") "mysql" else dbType
            val prefix = "database.$dbConfig"

            when (dbType) {
                "mysql", "mariadb" -> {
                    val host = config.getString("$prefix.host")
                    val port = config.getInt("$prefix.port")
                    val database = config.getString("$prefix.database")
                    val user = config.getString("$prefix.user")
                    val password = config.getString("$prefix.password")
                    if (dbType == "mysql") MyDBC(host, port, database, user, password, logger, exceptionLogger)
                    else MariaDBC(host, port, database, user, password, logger, exceptionLogger)
                }

                "h2" -> {
                    val file = config.getString("$prefix.file")
                    H2DBC(plugin.dataFolder, file, logger, exceptionLogger)
                }

                else -> throw UnsupportedOperationException("invalid database type")
            }
        }
    }
}