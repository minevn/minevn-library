package net.minevn.libs.db

import java.io.InputStream
import java.sql.Connection

@Suppress("SqlSourceToSinkFlow", "unused")
class DBMigrator(
    private val dbConnection: Connection,
    private val logger: (String) -> Unit,
    private val resourceFectcher: (String) -> InputStream?,
    private val sqlPath: String,
    private val currentVersion: Int,
) {
    private fun getMaxVersion(): Int {
        (1..9999).forEach {
            val version = it.toString().padStart(4, '0')
            val path = "$sqlPath/$version.sql"
            resourceFectcher(path) ?: return it - 1
        }
        return 0
    }

    fun migrate(maxVersion: Int? = null) : Int {
        val latestVersion = maxVersion ?: getMaxVersion()
        if (currentVersion >= latestVersion) {
            logger("The schema is up to date.")
            return latestVersion
        }
        ((currentVersion + 1)..latestVersion).forEach { versionNum ->
            val version = versionNum.toString().padStart(4, '0')
            val path = "$sqlPath/$version.sql"
            val stream = resourceFectcher(path) ?: throw IllegalStateException("File not found: $path")
            stream.bufferedReader().use { file ->
                logger("Updating schema to version $version...")
                val sql = file.readText()
                dbConnection.createStatement().use { stm ->
                    sql.split(";")
                        .map { it.trim() }
                        .filter { it.isNotBlank() }
                        .forEach { stm.addBatch(it) }
                    stm.executeBatch()
                }
            }
        }
        logger("Updating schema successfully.")
        return latestVersion
    }
}