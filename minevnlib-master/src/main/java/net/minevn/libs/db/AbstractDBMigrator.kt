package net.minevn.libs.db

import java.io.InputStream
import java.sql.Connection

@Suppress("SqlSourceToSinkFlow", "unused")
abstract class AbstractDBMigrator(
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

    fun migrate(maxVersion: Int? = null): Int {
        val latestVersion = maxVersion ?: getMaxVersion()
        logger("Current schema version: $currentVersion")
        if (currentVersion >= latestVersion) {
            logger("The schema is up to date.")
            return currentVersion
        }
        dbConnection.createStatement().use { stm ->
            ((currentVersion + 1)..latestVersion).forEach { versionNum ->
                val version = versionNum.toString().padStart(4, '0')
                val path = "$sqlPath/$version.sql"
                val stream = resourceFectcher(path) ?: throw IllegalStateException("File not found: $path") // should not happen
                stream.bufferedReader().use { file ->
                    file.readText()
                        .split(";")
                        .map { it.trim() }
                        .filter { it.isNotBlank() }
                        .forEach { stm.addBatch(it) }
                    }
                }
            stm.executeBatch()
        }
        logger("Schema updated to version $latestVersion successfully.")
        return latestVersion
    }
}
