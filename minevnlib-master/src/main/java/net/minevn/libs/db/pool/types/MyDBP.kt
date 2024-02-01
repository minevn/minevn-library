package net.minevn.libs.db.pool.types

import com.zaxxer.hikari.HikariDataSource
import net.minevn.libs.db.pool.DatabasePool
import java.util.logging.Level

class MyDBP(
    val host: String, val port: Int, val database: String, val user: String, val password: String,
    logger: (String) -> Unit,
    exceptionLogger: (Level, String, Throwable) -> Unit,
    customDataSource: HikariDataSource?
) : DatabasePool(logger, exceptionLogger) {

    init {
        try {
            logger("Connecting to the database (MySQL)...")

            dataSource = (customDataSource ?: getDefaultDataSource()).apply {
                if (dataSourceClassName == null && jdbcUrl == null) {
                    jdbcUrl = "jdbc:mysql://$host:$port/$database"
                }
            }
            dataSource.connection.close()

            logger("Connected to the database (MySQL)")
        } catch (ex: Exception) {
            exceptionLogger(Level.SEVERE, "Could not connect to the database", ex)
            throw ex
        }
    }

    fun getDefaultDataSource() = HikariDataSource().apply {
        username = user
        password = this@MyDBP.password
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

    override fun getTypeName() = "mysql"
}