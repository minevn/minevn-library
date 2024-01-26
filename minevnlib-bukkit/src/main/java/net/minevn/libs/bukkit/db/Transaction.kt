package net.minevn.libs.bukkit.db

import net.minevn.libs.bukkit.MineVNPlugin
import net.minevn.libs.db.DataAccess
import java.sql.Connection
import kotlin.reflect.KClass

class Transaction(private val plugin: MineVNPlugin, private val connection: Connection) {
    fun <T : DataAccess> KClass<T>.getTxInstance() = plugin.getDAO(this, true).apply {
        txConnection = connection
        transactional = true
    }
}

fun transaction(plugin: MineVNPlugin, action: Transaction.() -> Unit) {
    val connection = plugin.dbPool!!.getConnection()
    connection.autoCommit = false
    val transaction = Transaction(plugin, connection)
    try {
        transaction.action()
    } catch (ex: Exception) {
        connection.rollback()
        throw ex
    } finally {
        connection.close()
    }
}