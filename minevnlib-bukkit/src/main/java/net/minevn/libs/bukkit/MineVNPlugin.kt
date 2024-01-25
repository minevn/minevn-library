package net.minevn.libs.bukkit

import net.minevn.libs.bukkit.db.DataAccess
import net.minevn.libs.bukkit.db.DataAccessPool
import net.minevn.libs.db.connection.DatabaseConnection
import net.minevn.libs.db.connection.types.H2DBC
import net.minevn.libs.db.connection.types.MariaDBC
import net.minevn.libs.db.connection.types.MyDBC
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Level
import kotlin.reflect.KClass

abstract class MineVNPlugin : JavaPlugin() {
    private var daoPool: DataAccessPool? = null

    /**
     * Database connection
     */
    var dbConnection: DatabaseConnection? = null
        private set(value) {
            field?.disconnect()
            field = value
            daoPool = if (value != null) DataAccessPool(value) else null
        }

    /**
     * Get the data access object
     *
     * @param type data access type
     * @return data access object
     */
    fun <T : DataAccess> getDAO(type: KClass<T>) = daoPool!!.getInstance(type)

    /**
     * Initialize the database connection
     *
     * Sample configuration:
     * ```yaml
     * database:
     *   engine: h2
     *   h2:
     *     file: dotman
     *   mysql:
     *     host: localhost
     *     port: 3306
     *     user: 'root'
     *     password: '123'
     *     database: dotman
     * ```
     *
     * @param dbType database type
     * @param config configuration
     */
    protected fun initDatabase(dbType: String, config: YamlConfiguration) {
        val logger: (String) -> Unit = this.logger::info
        val exceptionLogger: (Level, String, Throwable) -> Unit = this.logger::log
        val dbConfig = if (dbType == "mariadb") "mysql" else dbType
        val prefix = "database.$dbConfig"

        dbConnection = when (dbType) {
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
                H2DBC(dataFolder, file, logger, exceptionLogger)
            }

            else -> throw UnsupportedOperationException("invalid database type")
        }
    }
}