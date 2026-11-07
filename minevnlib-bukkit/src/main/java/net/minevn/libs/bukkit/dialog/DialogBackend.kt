package net.minevn.libs.bukkit.dialog

import org.bukkit.entity.Player

/**
 * Cách gửi dialog theo loại server. Chỉ dùng nội bộ, plugin khác gọi qua [DialogAPI].
 *
 * Để public vì backend Paper nằm ở module minevnlib-paper.
 */
interface DialogBackend {
    /**
     * @param player player cần kiểm tra
     * @return true nếu client của player hiển thị được dialog
     */
    fun isSupported(player: Player): Boolean

    /**
     * Mở dialog, thay dialog đang mở (nếu có).
     *
     * @param player player được mở dialog
     * @param dialog dialog cần mở
     */
    fun show(player: Player, dialog: Dialog)

    /**
     * Đóng dialog đang mở, kể cả màn "Waiting for Server".
     *
     * @param player player cần đóng dialog
     */
    fun clear(player: Player)

    /**
     * Bỏ qua click callback cho tới khi mở khoá hoặc có dialog mới.
     *
     * @param player player cần khoá dialog
     */
    fun lock(player: Player)

    /**
     * Mở khoá dialog. Gọi được từ thread bất kỳ.
     *
     * @param player player cần mở khoá dialog
     */
    fun unlock(player: Player)
}
