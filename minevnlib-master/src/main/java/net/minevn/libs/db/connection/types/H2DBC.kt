package net.minevn.libs.db.connection.types

import com.zaxxer.hikari.HikariDataSource
import net.minevn.libs.db.connection.DatabaseConnection
import java.io.File
import java.util.logging.Level

class H2DBC (
    folder: File,
    fileName: String,
    logger: (String) -> Unit,
    exceptionLogger: (Level, String, Throwable) -> Unit
) : DatabaseConnection(logger, exceptionLogger) {

    init {
        try {
            val file = File(folder, fileName)
            logger("Connecting to the database (H2)...")

            dataSource = HikariDataSource().apply {
                addDataSourceProperty("url", "jdbc:h2:${file.absolutePath}")
                dataSourceClassName = "org.h2.jdbcx.JdbcDataSource"
                username = ""
                password = ""
                keepaliveTime = 60000L
            }
            connection = dataSource.connection

            logger("Connected to the database (H2)")
        } catch (ex: Exception) {
            exceptionLogger(Level.SEVERE, "Could not connect to the database", ex)
            throw ex
        }
    }

    override fun getTypeName() = "h2"
}