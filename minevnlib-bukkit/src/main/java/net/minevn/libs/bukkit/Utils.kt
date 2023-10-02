package net.minevn.libs.bukkit

import org.bukkit.ChatColor

fun List<String>.color() = map { it.color() }

fun String.color(): String = ChatColor.translateAlternateColorCodes('&', this)