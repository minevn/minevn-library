package net.minevn.libs.bukkit.dialog.via

import com.viaversion.viaversion.api.Via
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion
import net.minevn.libs.bukkit.MineVNLib
import net.minevn.libs.bukkit.dialog.AfterAction
import net.minevn.libs.bukkit.dialog.Dialog
import net.minevn.libs.bukkit.dialog.DialogBackend
import org.bukkit.entity.Player

/**
 * Dialog cho server cũ hơn 1.21.6: dựng packet 1.21.6 qua ViaVersion, chỉ client 1.21.6+ xem được.
 * Callback nhận qua packet custom_click_action ([ViaClickHook]), hook lỗi thì quay về lệnh ẩn.
 */
internal class ViaDialogBackend : DialogBackend {
    private val plugin = MineVNLib.instance
    private val sessions = CallbackSessions(plugin.logger)
    private val clickHook: ViaClickHook?

    init {
        // server 1.21.6+ không đi qua protocol 1.21.5 -> 1.21.6 của Via nên không dựng packet được
        if (Via.getAPI().serverVersion.lowestSupportedProtocolVersion().newerThanOrEqualTo(ProtocolVersion.v1_21_6)) {
            throw UnsupportedOperationException("Server 1.21.6+ cần dialog API của Paper (Paper 1.21.7+)")
        }

        clickHook = ViaClickHook(plugin, sessions::onCustomClick).takeIf { it.install() }
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
                token, separator, encoder.keys, encoder.booleans, encoder.handlers.toList(),
                closeOnUse = dialog.afterAction != AfterAction.NONE,
            ))
        }
        ViaDialogSender.show(player, tag)
    }

    override fun clear(player: Player) {
        sessions.remove(player)
        if (isSupported(player)) {
            ViaDialogSender.clear(player)
        }
    }

    /** Trả lại handler gốc của Via (cho /reload). */
    fun disable() {
        clickHook?.uninstall()
    }
}
