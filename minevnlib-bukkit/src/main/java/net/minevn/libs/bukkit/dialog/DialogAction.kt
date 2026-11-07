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
     *
     * - Giá trị input đã được kiểm tra theo dialog ([DialogResponse.sanitize]), click có giá trị sai bị bỏ qua
     * - Dialog [AfterAction.WAIT_FOR_RESPONSE]: client hiện màn "Waiting for Server" sau khi bấm,
     *   handler cần mở dialog mới hoặc gọi [DialogAPI.clear], nếu không player phải tự bấm nút quay lại
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

        /**
         * Kiểm tra giá trị client gửi về theo input đã gửi đi. Dùng chung cho mọi backend.
         *
         * Client bị sửa gửi được giá trị bất kỳ, không theo giới hạn của dialog:
         * - Text: cắt theo số dòng cho phép (mặc định 1 dòng) rồi theo maxLength
         * - Bool: phải là onTrue hoặc onFalse
         * - SingleOption: phải là id của 1 option trong dialog
         * - NumberRange: phải là số, kẹp vào khoảng start..end, bỏ phần .0 như [formatNumber]
         *
         * @return null nếu giá trị không hợp lệ
         */
        @JvmStatic
        fun sanitize(input: DialogInput, value: String): String? = when (input) {
            is DialogInput.Text -> {
                // client tính max_lines cả dòng tự ngắt, nên giá trị hợp lệ không bao giờ vượt số dòng này
                val maxLines = input.multiline?.let { it.maxLines ?: Int.MAX_VALUE } ?: 1
                value.lineSequence().take(maxLines).joinToString("\n").take(input.maxLength)
            }
            is DialogInput.Bool -> value.takeIf { it == input.onTrue || it == input.onFalse }
            is DialogInput.SingleOption -> value.takeIf { id -> input.options.any { it.id == id } }
            is DialogInput.NumberRange -> value.toFloatOrNull()
                ?.takeUnless { it.isNaN() }
                ?.let { formatNumber(it.coerceIn(minOf(input.start, input.end), maxOf(input.start, input.end))) }
        }

        /**
         * Lấy giá trị của toàn bộ input qua [sanitize]. Dùng chung cho mọi backend.
         * Client hợp lệ luôn gửi đủ và đúng giá trị, nên thiếu hoặc sai 1 input thì bỏ cả click.
         *
         * @param raw giá trị client gửi cho 1 input, null nếu không có
         * @return null nếu có input thiếu hoặc không hợp lệ
         */
        @JvmStatic
        fun sanitizeAll(inputs: List<DialogInput>, raw: (DialogInput) -> String?): Map<String, String>? {
            val values = LinkedHashMap<String, String>()
            for (input in inputs) {
                values[input.key] = raw(input)?.let { sanitize(input, it) } ?: return null
            }
            return values
        }
    }
}
