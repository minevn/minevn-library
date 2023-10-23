package net.minevn.libs

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

class SQLBuilder<T>(val sql: String, val connection: Connection) {
    lateinit var statementAction: PreparedStatement.() -> Unit
    lateinit var resultSetAction: ResultSet.() -> T
}

inline fun <T> String.sqlFetch(
    connection: Connection,
    statement: PreparedStatement.() -> Unit,
    block: ResultSet.() -> T
): T {
    var ps: PreparedStatement? = null
    var resultSet: ResultSet? = null
    try {
        ps = connection.prepareStatement(this)
        ps.statement()
        resultSet = ps.executeQuery()
        return resultSet.block()
    } finally {
        resultSet?.close()
        ps?.close()
    }
}

inline fun <T> String.sqlExecute(
    connection: Connection,
    statement: PreparedStatement.() -> Unit,
) {
    var ps: PreparedStatement? = null
    try {
        ps = connection.prepareStatement(this)
        ps.statement()
        ps.executeUpdate()
    } finally {
        ps?.close()
    }
}

fun test() {
    lateinit var connection: Connection

    val abc = """
        SELECT * FROM `dotman_napthe_log`
        WHERE `uuid` = ?
        ORDER BY `time` DESC
        LIMIT 1
    """.trimIndent()
        .sqlFetch(connection, {
            setString(1, "uuid")
        }, {
            getString("name")
        })
}