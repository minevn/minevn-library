package net.minevn.libs.db.connection.types

import com.zaxxer.hikari.HikariDataSource
import net.minevn.libs.db.connection.DatabaseConnection
import java.util.logging.Level

class MyDBC(
    host: String, port: Int, database: String, user: String, password: String,
    logger: (String) -> Unit,
    exceptionLogger: (Level, String, Throwable) -> Unit
) : DatabaseConnection(logger, exceptionLogger) {
    init {
        try {
            logger("Connecting to the database (MySQL)...")

            dataSource = HikariDataSource().apply {
                jdbcUrl = "jdbc:mysql://$host:$port/$database"
                username = user
                setPassword(password)
                maximumPoolSize = 12
                minimumIdle = 12
                maxLifetime = 1800000
                keepaliveTime = 60000L
                connectionTimeout = 20000
                addDataSourceProperty("cachePrepStmts", "true")
                addDataSourceProperty("prepStmtCacheSize", "250")
                addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
                addDataSourceProperty("useServerPrepStmts", "true")
                addDataSourceProperty("useLocalSessionState", "true")
                addDataSourceProperty("useLocalTransactionState", "true")
                addDataSourceProperty("rewriteBatchedStatements", "true")
                addDataSourceProperty("cacheResultSetMetadata", "true")
                addDataSourceProperty("cacheServerConfiguration", "true")
                addDataSourceProperty("elideSetAutoCommits", "true")
                addDataSourceProperty("maintainTimeStats", "false")
            }
            connection = dataSource.connection

            logger("Connected to the database (MySQL)")
        } catch (ex: Exception) {
            exceptionLogger(Level.SEVERE, "Could not connect to the database", ex)
            throw ex
        }
    }

    override fun getTypeName() = "mysql"
}