package net.minevn.libs.bukkit.dialog.via

import net.minevn.libs.bukkit.MineVNLib
import net.minevn.libs.bukkit.dialog.Dialog
import net.minevn.libs.bukkit.dialog.DialogBackend
import net.minevn.libs.bukkit.dialog.DialogClickGuard
import org.bukkit.entity.Player

/**
 * Dialog cho server cũ hơn 1.21.6: dựng packet 1.21.6 qua ViaVersion, chỉ client 1.21.6+ xem được.
 * Callback nhận qua packet custom_click_action ([ViaClickHook]), hook lỗi thì quay về lệnh ẩn.
 */
internal class ViaDialogBackend : DialogBackend {
    private val plugin = MineVNLib.instance
    private val guard = DialogClickGuard()
    private val sessions = CallbackSessions(plugin.logger, guard)
    private val clickHook: ViaClickHook?

    init {
        clickHook = ViaClickHook(plugin, guard, sessions::onCustomClick).takeIf { it.install() }
        val callbackRoute = if (clickHook != null) "packet custom_click_action" else "lệnh /${DialogEncoder.CALLBACK_COMMAND}"
        plugin.logger.info("Callback dialog qua $callbackRoute")
        plugin.server.pluginManager.registerEvents(sessions, plugin)
    }

    override fun isSupported(player: Player) = ViaDialogSender.isSupported(player)

    override fun show(player: Player, dialog: Dialog) {
        val token = sessions.newToken()
        val separator = sessions.newSeparator()
        val encoder = DialogEncoder(dialog, token, separator, clickHook != null)
        val tag = encoder.encode()

        if (encoder.handlers.isEmpty()) {
            sessions.remove(player)
        } else {
            sessions.put(player, CallbackSessions.Session(
                token, separator, dialog.inputs, encoder.handlers.toList(), dialog.afterAction,
                encoder.textHandlers.toSet(),
            ))
        }
        guard.onShow(player)
        ViaDialogSender.show(player, tag)
    }

    override fun clear(player: Player) {
        sessions.remove(player)
        if (isSupported(player)) {
            ViaDialogSender.clear(player)
        }
        guard.closeWaitingScreen(player)
    }

    override fun lock(player: Player) = guard.lock(player.uniqueId)

    override fun unlock(player: Player) = guard.unlock(player.uniqueId)

    /** Trả lại handler gốc của Via (cho /reload). */
    fun disable() {
        clickHook?.uninstall()
    }
}
