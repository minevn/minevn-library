package net.minevn.libs.bukkit.db

import net.minevn.libs.bukkit.MineVNPlugin
import net.minevn.libs.db.Transaction
import org.bukkit.Bukkit

fun transactional(plugin: MineVNPlugin, action: Transaction.() -> Unit) {
    if (Bukkit.isPrimaryThread()) {
        throw IllegalStateException("Cannot run transactional code on the main thread")
    }
    net.minevn.libs.db.transactional(plugin.dbPool!!.getConnection(), action)
}