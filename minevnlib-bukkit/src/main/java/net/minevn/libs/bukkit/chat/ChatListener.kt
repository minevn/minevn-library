package net.minevn.libs.bukkit.chat

import net.minevn.libs.bukkit.data.PlayerData

class ChatListener(private val playerData: PlayerData, val fallback: ChatListenerMessage.() -> Unit) {
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
