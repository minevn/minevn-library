package net.minevn.libs.bukkit.db

import net.minevn.libs.db.connection.DatabaseConnection
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Statement
import kotlin.reflect.KClass
import kotlin.reflect.cast

abstract class DataAccess {
    lateinit var connection: DatabaseConnection
    var transactional = false

    /**
     * Initialize the PreparedStatement with the given SQL statement
     */
    protected fun <R> String.statement(action: PreparedStatement.() -> R) : R {
        val dbConn = connection.getConnection()
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
        val dbConn = connection.getConnection()
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

class DataAccessPool(private val databaseConnection: DatabaseConnection) {
    private var instanceList = mutableMapOf<KClass<out DataAccess>, DataAccess>()

    fun <T : DataAccess> getInstance(type: KClass<T>): T {
        val dbType = databaseConnection.getTypeName()
        var instance = instanceList[type]
        if (instance == null) {
            val basePackage = type.java.`package`.name
            val daoClass = Class.forName("$basePackage.$dbType.${type.simpleName}Impl")
            instance = type.cast(daoClass.getDeclaredConstructor().newInstance())
            instance.connection = databaseConnection
            instanceList[type] = instance
        }
        return type.cast(instance)
    }
}