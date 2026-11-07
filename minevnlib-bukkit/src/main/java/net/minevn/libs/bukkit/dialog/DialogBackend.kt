package net.minevn.libs.bukkit.dialog

import org.bukkit.entity.Player

/**
 * Cách gửi dialog theo loại server. Chỉ dùng nội bộ, plugin khác gọi qua [DialogAPI].
 *
 * Để public vì backend Paper nằm ở module minevnlib-paper.
 */
interface DialogBackend {
    fun isSupported(player: Player): Boolean

    fun show(player: Player, dialog: Dialog)

    fun clear(player: Player)

    fun lock(player: Player)

    fun unlock(player: Player)
}
