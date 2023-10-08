package net.minevn.libs.bukkit.data

import net.minevn.libs.bukkit.chat.ChatListener
import org.bukkit.entity.Player

class PlayerData(val player: Player) {
    init {
        playerData[player] = this
    }

    var chatListener: ChatListener? = null

    fun destroy() {
        playerData.remove(player)
    }

    companion object {
        private val playerData = mutableMapOf<Player, PlayerData>()

        operator fun get(player: Player): PlayerData? {
            return playerData[player]
        }
    }
}