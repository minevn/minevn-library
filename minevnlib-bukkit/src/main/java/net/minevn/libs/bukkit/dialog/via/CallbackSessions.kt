package net.minevn.libs.bukkit.dialog.via

import com.viaversion.nbt.tag.ByteTag
import com.viaversion.nbt.tag.CompoundTag
import com.viaversion.nbt.tag.NumberTag
import com.viaversion.nbt.tag.StringTag
import com.viaversion.nbt.tag.Tag
import net.minevn.libs.bukkit.dialog.AfterAction
import net.minevn.libs.bukkit.dialog.DialogClickGuard
import net.minevn.libs.bukkit.dialog.DialogInput
import net.minevn.libs.bukkit.dialog.DialogResponse
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.security.SecureRandom
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Giữ callback của dialog đang mở, mỗi player tối đa 1 dialog (client chỉ hiện 1 dialog một lúc).
 * Nhận callback từ 2 đường:
 * - packet custom_click_action qua [ViaClickHook] ([onCustomClick])
 * - lệnh ẩn khi không hook được, bắt ở PlayerCommandPreprocessEvent để lấy nguyên chuỗi gốc
 *   (Bukkit tách args theo dấu cách)
 */
internal class CallbackSessions(private val logger: Logger, private val guard: DialogClickGuard) : Listener {
    class Session(
        val token: String,
        val separator: String,
        /** theo thứ tự giá trị trong lệnh callback */
        val inputs: List<DialogInput>,
        val handlers: List<(DialogResponse) -> Unit>,
        val afterAction: AfterAction,
    )

    private val sessions = ConcurrentHashMap<UUID, Session>()
    private val random = SecureRandom()

    fun newToken() = randomString(TOKEN_LENGTH)

    fun newSeparator() = "~" + randomString(SEPARATOR_RANDOM_LENGTH) + "~"

    fun put(player: Player, session: Session) {
        sessions[player.uniqueId] = session
    }

    fun remove(player: Player) {
        sessions.remove(player.uniqueId)
    }

    /** id có dạng minevnlib:cb/<token>/<index>, payload là compound key -> giá trị input. */
    fun onCustomClick(uuid: UUID, id: String, payload: Tag?) {
        val player = Bukkit.getPlayer(uuid) ?: return
        val parts = id.removePrefix(ViaClickHook.ID_PREFIX).split('/')
        if (parts.size != 2) {
            return
        }
        val index = parts[1].toIntOrNull() ?: return
        val session = sessions[uuid]?.takeIf { it.token == parts[0] } ?: return

        val compound = payload as? CompoundTag
        // boolean trả theo on_true / on_false như đường lệnh
        val values = DialogResponse.sanitizeAll(session.inputs) { input ->
            when (val tag = compound?.get(input.key)) {
                is StringTag -> tag.value
                is ByteTag -> (input as? DialogInput.Bool)
                    ?.let { if (tag.asBoolean()) it.onTrue else it.onFalse }
                    ?: tag.asBoolean().toString()
                is NumberTag -> DialogResponse.formatNumber(tag.asFloat())
                else -> null
            }
        } ?: return
        handle(player, session, index, values)
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    fun onCommand(event: PlayerCommandPreprocessEvent) {
        val message = event.message
        if (!message.startsWith(COMMAND_PREFIX, ignoreCase = true) && !message.equals(COMMAND_BARE, ignoreCase = true)) {
            return
        }
        event.isCancelled = true

        val player = event.player
        if (!guard.allowClick(player.uniqueId)) {
            return
        }
        val rest = message.substring(minOf(message.length, COMMAND_PREFIX.length))
        val spaceIndex = rest.indexOf(' ')
        val id = if (spaceIndex < 0) rest else rest.substring(0, spaceIndex)
        val payload = if (spaceIndex < 0) "" else rest.substring(spaceIndex + 1)

        val token = id.substringBefore('.')
        val index = id.substringAfter('.', "").toIntOrNull() ?: return
        val session = sessions[player.uniqueId]?.takeIf { it.token == token } ?: return
        val values = parseValues(session, payload) ?: return
        handle(player, session, index, values)
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        sessions.remove(event.player.uniqueId)
        guard.forget(event.player.uniqueId)
    }

    private fun handle(player: Player, session: Session, index: Int, values: Map<String, String>) {
        val handler = session.handlers.getOrNull(index) ?: return
        // dialog đóng sau khi bấm thì callback chỉ chạy 1 lần
        if (session.afterAction != AfterAction.NONE) {
            sessions.remove(player.uniqueId, session)
        }
        guard.onClick(player, session.afterAction)

        try {
            handler(DialogResponse(player, values))
        } catch (exception: Exception) {
            logger.log(Level.SEVERE, "Lỗi khi xử lý callback dialog của ${player.name}", exception)
            guard.closeWaitingScreen(player)
        }
    }

    private fun parseValues(session: Session, payload: String): Map<String, String>? {
        if (session.inputs.isEmpty()) {
            return emptyMap()
        }
        if (!payload.startsWith(session.separator)) {
            return null
        }
        val parts = payload.substring(session.separator.length).split(session.separator)
        if (parts.size != session.inputs.size) {
            return null
        }
        val raw = session.inputs.map { it.key }.zip(parts).toMap()
        return DialogResponse.sanitizeAll(session.inputs) { raw[it.key] }
    }

    private fun randomString(length: Int) = buildString(length) {
        repeat(length) { append(ALPHABET[random.nextInt(ALPHABET.length)]) }
    }

    companion object {
        private const val COMMAND_BARE = "/" + DialogEncoder.CALLBACK_COMMAND
        private const val COMMAND_PREFIX = "$COMMAND_BARE "
        private const val ALPHABET = "abcdefghijklmnopqrstuvwxyz0123456789"
        private const val TOKEN_LENGTH = 10
        private const val SEPARATOR_RANDOM_LENGTH = 4
    }
}
