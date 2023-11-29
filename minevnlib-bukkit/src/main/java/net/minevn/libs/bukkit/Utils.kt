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

val colorCodes = "abcdefklmr0123456789".toList()

fun String.split(maxLength: Int) = mutableListOf<String>().apply {
    var current: StringBuilder? = null
    var colorLength = 0

    this@split.forEachIndexed { index, char ->
        if (current == null) {
            current = StringBuilder().append(char)
        } else {
            current!!.append(char)
        }

        if (colorCodes.contains(char) && index > 0 && this@split[index - 1] == '§') {
            colorLength += 2
        }

        if (char != ' ') {
            return@forEachIndexed
        }

        if (current!!.length - colorLength >= maxLength) {
            val lastColor = if (isEmpty()) "" else ChatColor.getLastColors(last())
            add("$lastColor${current.toString().trim()}")
            current = null
            colorLength = 0
        }
    }

    current?.let { add(it.toString().trim()) }
}
