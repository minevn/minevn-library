package net.minevn.libs.db.connection.types

import com.zaxxer.hikari.HikariDataSource
import net.minevn.libs.db.connection.DatabaseConnection
import java.util.logging.Level

class MariaDBC(
    host: String, port: Int, database: String, user: String, password: String,
    logger: (String) -> Unit,
    exceptionLogger: (Level, String, Throwable) -> Unit
) : DatabaseConnection(logger, exceptionLogger) {
    init {
        try {
            logger("Connecting to the database (MariaDB)...")

            dataSource = HikariDataSource().apply {
                addDataSourceProperty("url", "jdbc:mariadb://$host:$port/$database")
                dataSourceClassName = "org.mariadb.jdbc.MariaDbDataSource"
                username = user
                setPassword(password)
                keepaliveTime = 60000L
            }
            connection = dataSource.connection

            logger("Connected to the database (MariaDB)")
        } catch (ex: Exception) {
            exceptionLogger(Level.SEVERE, "Could not connect to the database", ex)
            throw ex
        }
    }


    override fun getTypeName() = "mysql"
}