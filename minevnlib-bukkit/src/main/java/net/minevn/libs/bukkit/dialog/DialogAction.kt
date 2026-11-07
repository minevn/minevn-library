package net.minevn.libs.bukkit.dialog

import org.bukkit.entity.Player

/** Việc chạy khi player bấm nút hoặc click trong chữ của dialog. */
sealed class DialogAction {
    /**
     * Mở liên kết.
     *
     * @param url địa chỉ cần mở
     */
    class OpenUrl(val url: String) : DialogAction()

    /**
     * Chạy lệnh tĩnh, không có giá trị input.
     *
     * @param command lệnh cần chạy
     */
    class RunCommand(val command: String) : DialogAction()

    /**
     * Điền lệnh vào ô chat, player tự gửi.
     *
     * @param command lệnh cần điền
     */
    class SuggestCommand(val command: String) : DialogAction()

    /**
     * Chép chữ vào clipboard của player.
     *
     * @param value nội dung cần chép
     */
    class CopyToClipboard(val value: String) : DialogAction()

    /**
     * Gọi về plugin kèm giá trị của toàn bộ input. Handler chạy trên main thread.
     *
     * - Giá trị input đã được kiểm tra theo dialog ([DialogResponse.sanitize]), click có giá trị sai bị bỏ qua
     * - Dialog [AfterAction.WAIT_FOR_RESPONSE]: client hiện màn "Waiting for Server" sau khi bấm,
     *   handler cần mở dialog mới hoặc gọi [DialogAPI.clear], nếu không player phải tự bấm nút quay lại
     *
     * @param handler hàm nhận player đã bấm và giá trị input
     */
    class Callback(val handler: (DialogResponse) -> Unit) : DialogAction()
}

/**
 * Dữ liệu gửi về [DialogAction.Callback].
 *
 * @param player player đã bấm
 * @param values key của input -> giá trị đã kiểm tra, rỗng nếu là click trong chữ
 */
class DialogResponse(val player: Player, val values: Map<String, String>) {
    /**
     * @param key khoá của input
     * @return giá trị dạng chuỗi, null nếu không có
     */
    fun string(key: String): String? = values[key]

    /**
     * Chỉ nhận chuỗi `true` / `false`, nên [DialogInput.Bool] đặt onTrue / onFalse khác thì dùng [string].
     *
     * @param key khoá của input
     * @return giá trị dạng boolean, null nếu không có hoặc không phải `true` / `false`
     */
    fun boolean(key: String): Boolean? = values[key]?.toBooleanStrictOrNull()

    /**
     * @param key khoá của input
     * @return giá trị dạng số thực, null nếu không có hoặc không phải số
     */
    fun float(key: String): Float? = values[key]?.toFloatOrNull()

    /**
     * Bỏ phần thập phân của giá trị, không làm tròn.
     *
     * @param key khoá của input
     * @return giá trị dạng số nguyên, null nếu không có hoặc không phải số
     */
    fun int(key: String): Int? = float(key)?.toInt()

    companion object {
        /**
         * Số nguyên thì bỏ phần .0, để plugin dùng toInt / toFloat đều được. Dùng chung cho mọi backend.
         *
         * @param value số cần đổi
         * @return số dạng chuỗi
         */
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
         * @param input input đã gửi đi
         * @param value giá trị client gửi về
         * @return giá trị đã kiểm tra, null nếu không hợp lệ
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
         * @param inputs các input đã gửi đi
         * @param raw giá trị client gửi cho 1 input, null nếu không có
         * @return key của input -> giá trị đã kiểm tra, null nếu có input thiếu hoặc không hợp lệ
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
