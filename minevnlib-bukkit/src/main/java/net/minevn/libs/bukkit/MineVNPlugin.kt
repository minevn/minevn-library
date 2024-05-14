package net.minevn.libs.bukkit

import com.zaxxer.hikari.HikariDataSource
import net.minevn.libs.db.DataAccess
import net.minevn.libs.db.DataAccessPool
import net.minevn.libs.db.pool.DatabasePool
import net.minevn.libs.db.pool.types.H2DBP
import net.minevn.libs.db.pool.types.MariaDBP
import net.minevn.libs.db.pool.types.MyDBP
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Level
import kotlin.reflect.KClass

abstract class MineVNPlugin : JavaPlugin() {
    private var daoPool: DataAccessPool? = null

    /**
     * Database connection
     */
    var dbPool: DatabasePool? = null
        private set(value) {
            field?.disconnect()
            field = value
            daoPool = if (value != null) DataAccessPool(value, classLoader) else null
        }

    /**
     * Get the data access object
     *
     * @param type data access type
     * @return data access object
     */
    fun <T : DataAccess> getDAO(type: KClass<T>) =
        daoPool!!.getInstance(type)

    /**
     * Initialize the database connection
     *
     * Sample configuration:
     * ```yaml
     * engine: h2
     * h2:
     *   file: dotman
     * mysql:
     *   host: localhost
     *   port: 3306
     *   user: 'root'
     *   password: '123'
     *   database: dotman
     * ```
     *
     * @param dbType database type
     * @param config configuration
     */
    protected fun initDatabase(config: ConfigurationSection, customDataSource: HikariDataSource? = null) {
        dbPool = null
        val dbType = config.getString("engine", "h2")

        val logger: (String) -> Unit = this.logger::info
        val exceptionLogger: (Level, String, Throwable) -> Unit = this.logger::log
        val prefix = if (dbType == "mariadb") "mysql" else dbType

        dbPool = when (dbType) {
            "mysql", "mariadb" -> {
                val host = config.getString("$prefix.host")
                val port = config.getInt("$prefix.port")
                val database = config.getString("$prefix.database")
                val user = config.getString("$prefix.user")
                val password = config.getString("$prefix.password")
                if (dbType == "mysql") MyDBP(host, port, database, user, password, logger, exceptionLogger, customDataSource)
                else MariaDBP(host, port, database, user, password, logger, exceptionLogger, customDataSource)
            }

            "h2" -> {
                val file = config.getString("$prefix.file")
                H2DBP(dataFolder, file, logger, exceptionLogger, customDataSource)
            }

            else -> throw UnsupportedOperationException("invalid database type")
        }
    }
}