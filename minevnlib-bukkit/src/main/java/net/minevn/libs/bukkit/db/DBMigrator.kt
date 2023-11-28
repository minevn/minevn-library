package net.minevn.libs.bukkit.db

import net.minevn.libs.db.AbstractDBMigrator
import org.bukkit.plugin.java.JavaPlugin
import java.sql.Connection

@Suppress("unused")
class DBMigrator(
    plugin: JavaPlugin, dbConnection: Connection, sqlPath: String, currentVersion: Int
) : AbstractDBMigrator(dbConnection, plugin.logger::info, plugin::getResource, sqlPath, currentVersion)