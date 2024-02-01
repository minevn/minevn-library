package net.minevn.libs.db

import java.sql.Connection

class Transaction(val connection: Connection)

private val currentTransaction = ThreadLocal<Transaction>()

fun getTransaction(): Transaction? = currentTransaction.get()

fun transactional(connection: Connection, action: Transaction.() -> Unit) {
    connection.autoCommit = false
    val transaction = Transaction(connection)
    currentTransaction.set(transaction)
    try {
        transaction.action()
        connection.commit()
    } catch (ex: Exception) {
        connection.rollback()
        throw ex
    } finally {
        connection.close()
        currentTransaction.remove()
    }
}