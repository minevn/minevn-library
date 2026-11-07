package net.minevn.libs.bukkit.dialog

import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Trạng thái click dialog theo từng player, dùng chung cho mọi backend:
 * - Giới hạn số click mỗi giây: mỗi click chạy handler trên main thread, client bị sửa gửi được hàng trăm click
 * - Nhớ player đang ở màn "Waiting for Server" (vừa bấm nút của dialog [AfterAction.WAIT_FOR_RESPONSE]).
 *   Packet clear_dialog chỉ đóng dialog, không gỡ được màn này, phải đóng inventory
 * - Nhớ player đang bị khoá dialog ([DialogAPI.lock]): click callback bị bỏ qua tới khi mở khoá hoặc có dialog mới
 *
 * Chỉ dùng nội bộ, để public vì backend Paper nằm ở module minevnlib-paper.
 *
 * @param clock trả thời gian hiện tại theo mili giây, test thay được để không phải chờ thật
 */
class DialogClickGuard internal constructor(private val clock: () -> Long) {
    /** Dùng đồng hồ hệ thống. */
    constructor() : this(System::currentTimeMillis)

    /** player -> {giây, số click trong giây đó} */
    private val clickRates = ConcurrentHashMap<UUID, LongArray>()
    private val waiting = ConcurrentHashMap.newKeySet<UUID>()
    private val locked = ConcurrentHashMap.newKeySet<UUID>()

    /**
     * Click có được chạy không, quá [MAX_CLICKS_PER_SECOND] click mỗi giây thì bỏ. Gọi được từ thread bất kỳ.
     *
     * @param uuid player vừa click
     * @return false nếu click vượt giới hạn
     */
    fun allowClick(uuid: UUID): Boolean {
        val second = clock() / 1000
        var count = 0L
        clickRates.compute(uuid) { _, rate ->
            val current = if (rate == null || rate[0] != second) {
                longArrayOf(second, 0)
            } else {
                rate
            }
            current[1]++
            count = current[1]
            current
        }
        return count <= MAX_CLICKS_PER_SECOND
    }

    /**
     * Player vừa bấm nút callback của dialog có [afterAction]. Gọi trước khi chạy handler,
     * và cả khi click bị bỏ vì dialog đang khoá, vì client vẫn chuyển màn hình.
     *
     * @param player player vừa bấm
     * @param afterAction after action của dialog
     */
    fun onClick(player: Player, afterAction: AfterAction) {
        if (afterAction == AfterAction.WAIT_FOR_RESPONSE) {
            waiting.add(player.uniqueId)
        }
    }

    /**
     * Dialog mới thay màn hình đang hiện, kể cả màn chờ, và không còn bị khoá.
     *
     * @param player player được mở dialog
     */
    fun onShow(player: Player) {
        waiting.remove(player.uniqueId)
        locked.remove(player.uniqueId)
    }

    /**
     * Bỏ qua click callback của player.
     *
     * @param uuid player cần khoá
     */
    fun lock(uuid: UUID) {
        locked.add(uuid)
    }

    /**
     * Nhận lại click callback của player.
     *
     * @param uuid player cần mở khoá
     */
    fun unlock(uuid: UUID) {
        locked.remove(uuid)
    }

    /**
     * @param uuid player cần kiểm tra
     * @return true nếu player đang bị khoá dialog
     */
    fun isLocked(uuid: UUID) = uuid in locked

    /**
     * Gỡ màn chờ nếu player đang ở đó. Đóng inventory thì client bỏ mọi màn hình đang hiện.
     *
     * @param player player cần gỡ màn chờ
     */
    fun closeWaitingScreen(player: Player) {
        if (waiting.remove(player.uniqueId)) {
            player.closeInventory()
        }
    }

    /**
     * Xoá toàn bộ trạng thái của player, gọi khi player thoát.
     *
     * @param uuid player cần xoá
     */
    fun forget(uuid: UUID) {
        clickRates.remove(uuid)
        waiting.remove(uuid)
        locked.remove(uuid)
    }

    companion object {
        const val MAX_CLICKS_PER_SECOND = 10
    }
}
