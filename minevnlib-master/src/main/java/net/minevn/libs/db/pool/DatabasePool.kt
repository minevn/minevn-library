package net.minevn.libs.db.pool

import com.zaxxer.hikari.HikariDataSource
import java.sql.SQLException
import java.util.logging.Level

abstract class DatabasePool(
    val logger: (String) -> Unit,
    val exceptionLogger: (Level, String, Throwable) -> Unit
) {
    protected lateinit var dataSource: HikariDataSource

    fun getConnection() = dataSource.connection

    abstract fun getTypeName(): String

    fun disconnect() {
        logger("Disconnecting from the ${getTypeName()} database...")
        try {
            if (::dataSource.isInitialized) dataSource.close()
        } catch (ex: SQLException) {
            exceptionLogger(Level.SEVERE, "Could not disconnect from the database", ex)
        }
    }
}
