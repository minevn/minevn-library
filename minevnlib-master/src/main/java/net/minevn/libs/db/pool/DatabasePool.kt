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
        try {
            if (::dataSource.isInitialized) {
                logger("Disconnecting from the ${getTypeName()} database...")
                dataSource.close()
                logger("Disconnected from the ${getTypeName()} database")
            }
        } catch (ex: SQLException) {
            exceptionLogger(Level.SEVERE, "Could not disconnect from the database", ex)
        }
    }
}
