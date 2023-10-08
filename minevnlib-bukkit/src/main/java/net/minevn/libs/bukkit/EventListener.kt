package net.minevn.libs.bukkit

import net.minevn.libs.bukkit.chat.ChatListener
import net.minevn.libs.bukkit.data.PlayerData
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class EventListener : Listener {
    init {
        MineVNLib.instance.server.pluginManager.registerEvents(this, MineVNLib.instance)
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        PlayerData(event.player)
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        PlayerData[event.player]?.destroy()
    }

    @EventHandler
    fun onChat(event: AsyncPlayerChatEvent) {
        val chatListener = PlayerData[event.player]?.chatListener ?: return
        event.isCancelled = true
        chatListener.onChat(ChatListener.ChatListenerMessage(event.message))
    }
}