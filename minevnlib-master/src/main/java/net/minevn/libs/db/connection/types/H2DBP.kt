package net.minevn.libs.db.connection.types

import com.zaxxer.hikari.HikariDataSource
import net.minevn.libs.db.connection.DatabasePool
import java.io.File
import java.util.logging.Level

class H2DBP (
    val folder: File,
    val fileName: String,
    logger: (String) -> Unit,
    exceptionLogger: (Level, String, Throwable) -> Unit,
    customDataSource: HikariDataSource?
) : DatabasePool(logger, exceptionLogger) {

    init {
        try {
            logger("Connecting to the database (H2)...")

            dataSource = (customDataSource ?: getDefaultDataSource()).apply {
                if (dataSourceClassName == null && jdbcUrl == null) {
                    val file = File(folder, fileName)
                    addDataSourceProperty("url", "jdbc:h2:${file.absolutePath}")
                    dataSourceClassName = "org.h2.jdbcx.JdbcDataSource"
                }
            }

            logger("Connected to the database (H2)")
        } catch (ex: Exception) {
            exceptionLogger(Level.SEVERE, "Could not connect to the database", ex)
            throw ex
        }
    }

    fun getDefaultDataSource() = HikariDataSource().apply {
        username = ""
        password = ""
        keepaliveTime = 60000L
    }

    override fun getTypeName() = "h2"
}