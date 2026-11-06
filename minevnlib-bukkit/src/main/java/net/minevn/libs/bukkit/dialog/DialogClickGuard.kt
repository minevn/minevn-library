package net.minevn.libs.bukkit.dialog

import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Trạng thái click dialog theo từng player, dùng chung cho mọi backend:
 * - Giới hạn số click mỗi giây: mỗi click chạy handler trên main thread, client bị sửa gửi được hàng trăm click
 * - Nhớ player đang ở màn "Waiting for Server" (vừa bấm nút của dialog [AfterAction.WAIT_FOR_RESPONSE]).
 *   Packet clear_dialog chỉ đóng dialog, không gỡ được màn này, phải đóng inventory
 *
 * Chỉ dùng nội bộ, để public vì backend Paper nằm ở module minevnlib-paper.
 */
class DialogClickGuard internal constructor(private val clock: () -> Long) {
    constructor() : this(System::currentTimeMillis)

    /** player -> {giây, số click trong giây đó} */
    private val clickRates = ConcurrentHashMap<UUID, LongArray>()
    private val waiting = ConcurrentHashMap.newKeySet<UUID>()

    /** Click có được chạy không, quá [MAX_CLICKS_PER_SECOND] click mỗi giây thì bỏ. Gọi được từ thread bất kỳ. */
    fun allowClick(uuid: UUID): Boolean {
        val second = clock() / 1000
        var count = 0L
        clickRates.compute(uuid) { _, rate ->
            (if (rate == null || rate[0] != second) longArrayOf(second, 0) else rate).also { count = ++it[1] }
        }
        return count <= MAX_CLICKS_PER_SECOND
    }

    /** Player vừa bấm nút callback của dialog có [afterAction]. Gọi trước khi chạy handler. */
    fun onClick(player: Player, afterAction: AfterAction) {
        if (afterAction == AfterAction.WAIT_FOR_RESPONSE) {
            waiting.add(player.uniqueId)
        }
    }

    /** Dialog mới thay màn hình đang hiện, kể cả màn chờ. */
    fun onShow(player: Player) {
        waiting.remove(player.uniqueId)
    }

    /** Gỡ màn chờ nếu player đang ở đó. Đóng inventory thì client bỏ mọi màn hình đang hiện. */
    fun closeWaitingScreen(player: Player) {
        if (waiting.remove(player.uniqueId)) {
            player.closeInventory()
        }
    }

    fun forget(uuid: UUID) {
        clickRates.remove(uuid)
        waiting.remove(uuid)
    }

    companion object {
        const val MAX_CLICKS_PER_SECOND = 10
    }
}
