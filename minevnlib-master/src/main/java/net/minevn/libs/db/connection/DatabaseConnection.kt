package net.minevn.libs.db.connection

import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection
import java.sql.SQLException
import java.util.logging.Level

abstract class DatabaseConnection(
    val logger: (String) -> Unit,
    val exceptionLogger: (Level, String, Throwable) -> Unit
) {
    lateinit var connection: Connection protected set
    protected lateinit var dataSource: HikariDataSource

    abstract fun getTypeName(): String

    fun disconnect() {
        logger("Disconnecting from the ${getTypeName()} database...")
        try {
            if (::connection.isInitialized) connection.close()
            if (::dataSource.isInitialized) dataSource.close()
        } catch (ex: SQLException) {
            exceptionLogger(Level.SEVERE, "Could not disconnect from the database", ex)
        }
    }
}
