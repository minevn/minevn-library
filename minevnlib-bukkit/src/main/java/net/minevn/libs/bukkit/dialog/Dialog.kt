package net.minevn.libs.bukkit.dialog

import net.kyori.adventure.text.Component

/**
 * Model của dialog theo format 1.21.6 (data-driven dialog).
 * Chỉ cover phần gửi inline, không đụng registry / tag.
 */
sealed class Dialog(
    val title: Component,
    val externalTitle: Component?,
    val body: List<DialogBody>,
    val inputs: List<DialogInput>,
    val canCloseWithEscape: Boolean,
    val pause: Boolean,
    val afterAction: AfterAction,
)

/** Dialog thông báo, 1 nút duy nhất. */
class NoticeDialog(
    title: Component,
    val action: DialogButton? = null,
    body: List<DialogBody> = emptyList(),
    inputs: List<DialogInput> = emptyList(),
    externalTitle: Component? = null,
    canCloseWithEscape: Boolean = true,
    pause: Boolean = true,
    afterAction: AfterAction = AfterAction.CLOSE,
) : Dialog(title, externalTitle, body, inputs, canCloseWithEscape, pause, afterAction)

/** Dialog 2 nút có / không. */
class ConfirmationDialog(
    title: Component,
    val yes: DialogButton,
    val no: DialogButton,
    body: List<DialogBody> = emptyList(),
    inputs: List<DialogInput> = emptyList(),
    externalTitle: Component? = null,
    canCloseWithEscape: Boolean = true,
    pause: Boolean = true,
    afterAction: AfterAction = AfterAction.CLOSE,
) : Dialog(title, externalTitle, body, inputs, canCloseWithEscape, pause, afterAction)

/** Dialog nhiều nút xếp theo cột. */
class MultiActionDialog(
    title: Component,
    val actions: List<DialogButton>,
    val columns: Int = 2,
    val exitAction: DialogButton? = null,
    body: List<DialogBody> = emptyList(),
    inputs: List<DialogInput> = emptyList(),
    externalTitle: Component? = null,
    canCloseWithEscape: Boolean = true,
    pause: Boolean = true,
    afterAction: AfterAction = AfterAction.CLOSE,
) : Dialog(title, externalTitle, body, inputs, canCloseWithEscape, pause, afterAction) {
    init {
        require(actions.isNotEmpty()) { "MultiActionDialog cần ít nhất 1 action" }
        require(columns > 0) { "columns phải > 0" }
    }
}

enum class AfterAction(val id: String) {
    CLOSE("close"),
    NONE("none"),
    WAIT_FOR_RESPONSE("wait_for_response"),
}

class DialogButton(
    val label: Component,
    val action: DialogAction? = null,
    val tooltip: Component? = null,
    val width: Int? = null,
)

sealed class DialogBody {
    class PlainMessage(val contents: Component, val width: Int? = null) : DialogBody()

    /**
     * Như [PlainMessage] nhưng tự khai báo từng đoạn chữ, click và tooltip nằm ngay trong chữ.
     * Dùng khi Component không đủ: Adventure của server cũ chưa biết shadow color và click custom.
     */
    class RichText(val runs: List<DialogTextRun>, val width: Int? = null) : DialogBody()
}

/**
 * Một đoạn chữ của [DialogBody.RichText].
 *
 * @param font id font, null là font mặc định
 * @param color màu RGB trong khoảng 0..0xFFFFFF, null là mặc định
 * @param shadow false là tắt bóng chữ
 * @param click [DialogAction.Callback] trong chữ nhận [DialogResponse.values] rỗng vì click chữ không gửi được input.
 *   Click chữ cũng áp [Dialog.afterAction] như nút
 */
class DialogTextRun(
    val text: String,
    val font: String? = null,
    val color: Int? = null,
    val bold: Boolean = false,
    val shadow: Boolean = false,
    val click: DialogAction? = null,
    val tooltip: Component? = null,
) {
    // codec của client từ chối, ở đường ViaVersion player sẽ bị ngắt kết nối
    init {
        require(color == null || color in 0..0xFFFFFF) { "color phải là RGB trong khoảng 0..0xFFFFFF" }
        require(font == null || FONT_PATTERN.matches(font)) { "font không phải id hợp lệ: $font" }
    }

    companion object {
        // không dùng Key.parseable: server cũ có Adventure < 4.13 chưa có hàm này
        private val FONT_PATTERN = Regex("([a-z0-9_.-]+:)?[a-z0-9_./-]+")
    }
}

sealed class DialogInput(val key: String) {
    init {
        require(KEY_PATTERN.matches(key)) { "Input key không hợp lệ: $key (chỉ a-z, 0-9, _)" }
    }

    /**
     * Ô nhập chữ, mặc định 1 dòng.
     *
     * @param multiline cho nhập nhiều dòng, các dòng ngăn cách bằng `\n`
     * - Đường ViaVersion cần hook custom_click_action: ở đường lệnh ẩn, ký tự xuống dòng trong chat command
     *   sẽ bị server kick, nên [DialogAPI.show] ném [IllegalArgumentException]
     * - Packet callback tối đa 65536 byte, nên tránh đặt [maxLength] quá lớn
     */
    class Text(
        key: String,
        val label: Component,
        val initial: String = "",
        val maxLength: Int = 32,
        val width: Int? = null,
        val labelVisible: Boolean = true,
        val multiline: Multiline? = null,
    ) : DialogInput(key) {
        init {
            require(maxLength > 0) { "maxLength phải > 0" }
            // codec của client từ chối, ở đường ViaVersion player sẽ bị ngắt kết nối
            require(initial.length <= maxLength) { "initial dài hơn maxLength" }
        }

        class Multiline(val maxLines: Int? = null, val height: Int? = null) {
            // codec của client từ chối, ở đường ViaVersion player sẽ bị ngắt kết nối
            init {
                require(maxLines == null || maxLines > 0) { "maxLines phải > 0" }
                require(height == null || height in 1..512) { "height phải trong khoảng 1..512" }
            }
        }
    }

    class Bool(
        key: String,
        val label: Component,
        val initial: Boolean = false,
        val onTrue: String = "true",
        val onFalse: String = "false",
    ) : DialogInput(key)

    class SingleOption(
        key: String,
        val label: Component,
        val options: List<Option>,
        val width: Int? = null,
        val labelVisible: Boolean = true,
    ) : DialogInput(key) {
        init {
            require(options.isNotEmpty()) { "SingleOption cần ít nhất 1 option" }
        }

        class Option(val id: String, val display: Component? = null, val initial: Boolean = false)
    }

    class NumberRange(
        key: String,
        val label: Component,
        val start: Float,
        val end: Float,
        val step: Float? = null,
        val initial: Float? = null,
        val width: Int? = null,
        val labelFormat: String? = null,
    ) : DialogInput(key) {
        // codec của client từ chối, client cho phép start > end
        init {
            require(step == null || step > 0f) { "step phải > 0" }
            require(initial == null || initial in minOf(start, end)..maxOf(start, end)) {
                "initial nằm ngoài khoảng giữa start và end"
            }
        }
    }

    companion object {
        private val KEY_PATTERN = Regex("[a-z0-9_]+")
    }
}
