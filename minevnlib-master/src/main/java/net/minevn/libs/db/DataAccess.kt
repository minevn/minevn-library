package net.minevn.libs.db

import net.minevn.libs.db.pool.DatabasePool
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Statement
import kotlin.reflect.KClass
import kotlin.reflect.cast

abstract class DataAccess {
    lateinit var dbPool: DatabasePool

    /**
     * Initialize the PreparedStatement with the given SQL statement
     */
    protected fun <R> String.statement(action: PreparedStatement.() -> R) =
        statement(action, false)

    /**
     * Initialize the PreparedStatement with the given SQL statement, with the option to return generated keys
     */
    protected fun <R> String.statementWithKey(action: PreparedStatement.() -> R) : R =
        statement(action, true)

    private fun <R> String.statement(action: PreparedStatement.() -> R, generatedKey: Boolean) : R {
        val transaction = getTransaction()
        var transactional = true
        val connection = transaction?.connection ?: run {
            transactional = false
            dbPool.getConnection()
        }
        try {
            if (!generatedKey) return connection.prepareStatement(this).use { it.action() }
            return connection.prepareStatement(this, Statement.RETURN_GENERATED_KEYS).use { it.action() }
        } finally {
            if (!transactional) connection.close()
        }
    }

    /**
     * Process the ResultSet
     */
    protected fun <R> PreparedStatement.fetch(action: ResultSet.() -> R) =
        executeQuery().use { it.action() }

    /**
     * Iterate through all records in the ResultSet
     */
    protected fun <R> PreparedStatement.fetchIterate(action: ResultSet.() -> R) =
        fetch {
            while (next()) {
                action()
            }
        }

    @Synchronized
    protected fun PreparedStatement.update() = executeUpdate()

    /**
     * Map all records in the ResultSet to a list
     */
    protected fun <R> PreparedStatement.fetchRecords(action: ResultSet.() -> R): List<R> =
        fetch { generateSequence { if (next()) action() else null }.toList() }
}

class DataAccessPool(private val databaseConnection: DatabasePool, val loader: ClassLoader) {
    private var instanceList = mutableMapOf<KClass<out DataAccess>, DataAccess>()

    @Synchronized
    fun <T : DataAccess> getInstance(type: KClass<T>): T {
        val dbType = databaseConnection.getTypeName()
        var instance = instanceList[type]
        if (instance == null) {
            val basePackage = type.java.`package`.name
            val daoClass = Class.forName("$basePackage.$dbType.${type.simpleName}Impl", true, loader)
            instance = type.cast(daoClass.getDeclaredConstructor().newInstance())
            instance.dbPool = databaseConnection
            instanceList[type] = instance
        }
        return type.cast(instance)
    }
}