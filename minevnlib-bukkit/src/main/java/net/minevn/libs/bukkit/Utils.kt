package net.minevn.libs.bukkit

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player

fun List<String>.color() = map { it.color() }

fun String.color(): String = ChatColor.translateAlternateColorCodes('&', this)

fun runSync(action: Runnable) {
    if (Bukkit.isPrimaryThread()) {
        action.run()
    } else {
        Bukkit.getScheduler().runTask(MineVNLib.instance, action)
    }
}

fun String.parseJson() = JsonParser.parseString(this)!!

fun JsonElement.getOrNull() = if (isJsonNull) null else this

fun Player.sendMessages(messages: List<String>) = messages.forEach { sendMessage(it) }

fun Player.sendMessages(messages: Array<String>) = messages.forEach { sendMessage(it) }