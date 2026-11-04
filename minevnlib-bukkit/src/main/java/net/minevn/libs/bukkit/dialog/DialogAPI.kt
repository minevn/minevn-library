package net.minevn.libs.bukkit.dialog

import net.minevn.libs.bukkit.ViaVersionNotInstalledException
import net.minevn.libs.bukkit.dialog.via.ViaDialogBackend
import org.bukkit.Bukkit
import org.bukkit.entity.Player

/**
 * API dialog (tính năng của 1.21.6), dùng chung cho mọi phiên bản server. Chỉ gọi trên main thread.
 *
 * Chọn backend ở lần gọi đầu:
 * - Server có dialog API của Paper (Paper 1.21.7+): gọi API của Paper
 * - Server cũ hơn 1.21.6 có ViaVersion: dựng packet dialog qua ViaVersion cho client 1.21.6+
 * - Không có ViaVersion: ném [ViaVersionNotInstalledException]
 * - Server 1.21.6+ nhưng không có dialog API của Paper (Spigot, Paper 1.21.6): ném [UnsupportedOperationException]
 */
object DialogAPI {
    private const val PAPER_DIALOG_CLASS = "io.papermc.paper.dialog.Dialog"
    private const val PAPER_BACKEND_CLASS = "net.minevn.libs.paper.dialog.PaperDialogBackend"

    // lazy không giữ exception: server chưa hỗ trợ thì mỗi lần gọi đều thử lại và ném lại
    private val backend by lazy { createBackend() }
    private var viaBackend: ViaDialogBackend? = null

    /**
     * Client của player có hiển thị được dialog không. Client cũ hơn 1.21.6 cần fallback (GUI / chat).
     */
    @JvmStatic
    fun isSupported(player: Player) = backend.isSupported(player)

    /**
     * Mở dialog cho player, thay dialog đang mở (nếu có).
     *
     * @return false nếu client không hỗ trợ dialog
     * @throws IllegalArgumentException nếu dialog không hợp lệ (vd lệnh callback có thể quá 256 ký tự)
     */
    @JvmStatic
    fun show(player: Player, dialog: Dialog): Boolean {
        if (!backend.isSupported(player)) {
            return false
        }
        backend.show(player, dialog)
        return true
    }

    /**
     * Đóng dialog đang mở của player. Trên Paper cần 1.21.8+.
     */
    @JvmStatic
    fun clear(player: Player) {
        backend.clear(player)
    }

    internal fun disable() {
        viaBackend?.disable()
    }

    private fun createBackend(): DialogBackend {
        if (hasPaperDialogApi()) {
            // backend Paper biên dịch với paper-api 1.21.8 ở module khác, chỉ nạp khi server có API
            return Class.forName(PAPER_BACKEND_CLASS).getDeclaredConstructor().newInstance() as DialogBackend
        }
        // server 1.21.6+ không đi qua protocol 1.21.5 -> 1.21.6 của Via, cài ViaVersion cũng không dùng được
        if (isAtLeast1216(Bukkit.getBukkitVersion())) {
            throw UnsupportedOperationException("Server 1.21.6+ cần dialog API của Paper (Paper 1.21.7+)")
        }
        if (!Bukkit.getPluginManager().isPluginEnabled("ViaVersion")) {
            throw ViaVersionNotInstalledException()
        }
        return ViaDialogBackend().also { viaBackend = it }
    }

    /**
     * @param bukkitVersion dạng `1.21.8-R0.1-SNAPSHOT`, hoặc `26.3-R0.1-SNAPSHOT` từ khi Minecraft đổi cách đánh số
     */
    internal fun isAtLeast1216(bukkitVersion: String): Boolean {
        val numbers = bukkitVersion.substringBefore('-').split('.').map { it.toIntOrNull() ?: 0 }
        val (major, minor, patch) = numbers + listOf(0, 0, 0)
        return major > 1 || (major == 1 && (minor > 21 || (minor == 21 && patch >= 6)))
    }

    // không khởi tạo class: static field của Dialog truy cập registry
    private fun hasPaperDialogApi() = try {
        Class.forName(PAPER_DIALOG_CLASS, false, DialogAPI::class.java.classLoader)
        true
    } catch (_: ClassNotFoundException) {
        false
    }
}
