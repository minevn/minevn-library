package net.minevn.libs.db

import net.minevn.libs.db.connection.DatabasePool
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Statement
import kotlin.reflect.KClass
import kotlin.reflect.cast

abstract class DataAccess {
    var txConnection: Connection? = null
    lateinit var dbPool: DatabasePool
    var transactional = false

    /**
     * Initialize the PreparedStatement with the given SQL statement
     */
    protected fun <R> String.statement(action: PreparedStatement.() -> R) : R {
        val dbConn = txConnection ?: dbPool.getConnection()
        try {
            return dbConn.prepareStatement(this).use { it.action() }
        } finally {
            if (!transactional) dbConn.close()
        }
    }


    /**
     * Initialize the PreparedStatement with the given SQL statement, with the option to return generated keys
     */
    protected fun <R> String.statementWithKey(action: PreparedStatement.() -> R) : R {
        val dbConn = txConnection ?: dbPool.getConnection()
        try {
            return dbConn.prepareStatement(this, Statement.RETURN_GENERATED_KEYS).use { it.action() }
        } finally {
            if (!transactional) dbConn.close()
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

    /**
     * Map all records in the ResultSet to a list
     */
    protected fun <R> PreparedStatement.fetchRecords(action: ResultSet.() -> R): List<R> =
        fetch { generateSequence { if (next()) action() else null }.toList() }
}

class DataAccessPool(private val databaseConnection: DatabasePool) {
    private var instanceList = mutableMapOf<KClass<out DataAccess>, DataAccess>()
    private var transactionalInstanceList = mutableMapOf<KClass<out DataAccess>, DataAccess>()

    fun <T : DataAccess> getInstance(type: KClass<T>, transactional: Boolean): T {
        val targetList = if (transactional) transactionalInstanceList else instanceList
        val dbType = databaseConnection.getTypeName()
        var instance = targetList[type]
        if (instance == null) {
            val basePackage = type.java.`package`.name
            val daoClass = Class.forName("$basePackage.$dbType.${type.simpleName}Impl")
            instance = type.cast(daoClass.getDeclaredConstructor().newInstance())
            instance.dbPool = databaseConnection
            targetList[type] = instance
        }
        return type.cast(instance)
    }
}