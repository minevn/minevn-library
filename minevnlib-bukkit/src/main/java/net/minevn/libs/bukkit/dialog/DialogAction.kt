package net.minevn.libs.bukkit.dialog

import org.bukkit.entity.Player

sealed class DialogAction {
    class OpenUrl(val url: String) : DialogAction()

    /** Chạy lệnh tĩnh, không có giá trị input. */
    class RunCommand(val command: String) : DialogAction()

    class SuggestCommand(val command: String) : DialogAction()

    class CopyToClipboard(val value: String) : DialogAction()

    /**
     * Gọi về plugin kèm giá trị của toàn bộ input. Handler chạy trên main thread.
     */
    class Callback(val handler: (DialogResponse) -> Unit) : DialogAction()
}

class DialogResponse(val player: Player, val values: Map<String, String>) {
    fun string(key: String): String? = values[key]

    fun boolean(key: String): Boolean? = values[key]?.toBooleanStrictOrNull()

    fun float(key: String): Float? = values[key]?.toFloatOrNull()

    fun int(key: String): Int? = float(key)?.toInt()

    companion object {
        /** Số nguyên thì bỏ phần .0, để plugin dùng toInt / toFloat đều được. Dùng chung cho mọi backend. */
        @JvmStatic
        fun formatNumber(value: Float) =
            if (value == value.toLong().toFloat()) value.toLong().toString() else value.toString()
    }
}
