package net.minevn.libs.bukkit.dialog.via

import com.viaversion.nbt.tag.Tag
import com.viaversion.viaversion.api.Via
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler
import com.viaversion.viaversion.api.type.Types
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.Protocol1_21_5To1_21_6
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ServerboundPackets1_21_6
import net.minevn.libs.bukkit.dialog.DialogClickGuard
import net.minevn.libs.bukkit.runSync
import org.bukkit.plugin.Plugin
import java.util.UUID
import java.util.logging.Level

/**
 * Bắt packet custom_click_action (1.21.6) trước khi ViaVersion huỷ.
 *
 * Mặc định Protocol1_21_5To1_21_6 đăng ký cancelServerbound(CUSTOM_CLICK_ACTION) vì server < 1.21.6 không có packet này.
 * Hook ghi đè handler đó (override = true): đọc id + payload, đẩy về main thread rồi vẫn huỷ packet như cũ.
 * Id không thuộc namespace của thư viện thì chỉ huỷ, giữ nguyên hành vi gốc của Via.
 * Click quá giới hạn của [guard] bị bỏ ngay trên thread của Via, không đẩy về main thread.
 *
 * Can thiệp vào nội bộ ViaVersion, có thể hỏng khi nâng Via, nên lỗi lúc cài thì quay về callback bằng lệnh.
 */
internal class ViaClickHook(
    private val plugin: Plugin,
    private val guard: DialogClickGuard,
    private val onClick: (uuid: UUID, id: String, payload: Tag?) -> Unit,
) {
    var installed = false
        private set

    fun install(): Boolean {
        installed = try {
            val protocol = protocol() ?: run {
                plugin.logger.warning("Không tìm thấy Protocol1_21_5To1_21_6, dùng callback bằng lệnh")
                return false
            }
            protocol.registerServerbound(ServerboundPackets1_21_6.CUSTOM_CLICK_ACTION, null, PacketHandler { wrapper ->
                val id = wrapper.read(Types.STRING)
                val payload = wrapper.read(Types.CUSTOM_CLICK_ACTION_TAG)
                val uuid = wrapper.user().protocolInfo.uuid
                if (uuid != null && id.startsWith(ID_PREFIX) && guard.allowClick(uuid)) {
                    runSync { onClick(uuid, id, payload) }
                }
                wrapper.cancel()
            }, true)
            true
        } catch (exception: Throwable) {
            plugin.logger.log(Level.WARNING, "Không hook được custom_click_action, dùng callback bằng lệnh", exception)
            false
        }
        return installed
    }

    /** Trả lại handler huỷ packet như mặc định của Via (cho /reload). */
    fun uninstall() {
        if (!installed) {
            return
        }
        installed = false
        try {
            protocol()?.registerServerbound(ServerboundPackets1_21_6.CUSTOM_CLICK_ACTION, null, PacketHandler { it.cancel() }, true)
        } catch (exception: Throwable) {
            plugin.logger.log(Level.WARNING, "Không gỡ được hook custom_click_action", exception)
        }
    }

    // khai báo kiểu rõ ràng: kiểu suy ra mang @Nullable của checkerframework, không có trên classpath
    private fun protocol(): Protocol1_21_5To1_21_6? =
        Via.getManager().protocolManager.getProtocol(Protocol1_21_5To1_21_6::class.java)

    companion object {
        /** Id của action: minevnlib:cb/<token>/<index> */
        const val ID_PREFIX = "minevnlib:cb/"
    }
}
