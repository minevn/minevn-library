package net.minevn.libs.db.connection.types

import com.zaxxer.hikari.HikariDataSource
import net.minevn.libs.db.connection.DatabasePool
import java.util.logging.Level

class MariaDBP(
    val host: String, val port: Int, val database: String, val user: String, val password: String,
    logger: (String) -> Unit,
    exceptionLogger: (Level, String, Throwable) -> Unit,
    customDataSource: HikariDataSource?
) : DatabasePool(logger, exceptionLogger) {
    init {
        try {
            logger("Connecting to the database (MariaDB)...")

            dataSource = (customDataSource ?: getDefaultDataSource()).apply {
                if (dataSourceClassName == null && jdbcUrl == null) {
                    addDataSourceProperty("url", "jdbc:mariadb://$host:$port/$database")
                    dataSourceClassName = "org.mariadb.jdbc.MariaDbDataSource"
                }
            }

            logger("Connected to the database (MariaDB)")
        } catch (ex: Exception) {
            exceptionLogger(Level.SEVERE, "Could not connect to the database", ex)
            throw ex
        }
    }

    fun getDefaultDataSource() = HikariDataSource().apply {
        username = user
        setPassword(password)
        keepaliveTime = 60000L
    }

    override fun getTypeName() = "mysql"
}