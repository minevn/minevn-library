package net.minevn.libs.bukkit.chat

import net.minevn.libs.bukkit.data.PlayerData
import org.bukkit.entity.Player

class ChatListener(player: Player, val fallback: ChatListenerMessage.() -> Unit) {
    private val playerData = PlayerData[player]!!

    init {
        playerData.chatListener = this
    }

    fun onChat(message: ChatListenerMessage) {
        try {
            fallback(message)
        } finally {
            destroy()
        }
    }

    private fun destroy() {
        if (playerData.chatListener == this) {
            playerData.chatListener = null
        }
    }

    class ChatListenerMessage(val message: String)
}
