package net.minevn.libs.bukkit.dialog

import net.kyori.adventure.text.Component

/**
 * Model của dialog theo format 1.21.6 (data-driven dialog).
 * Chỉ cover phần gửi inline, không đụng registry / tag.
 *
 * @param title tiêu đề trên đầu dialog
 * @param externalTitle tên dùng cho nút dẫn tới dialog này, null thì dùng [title]
 * @param body các khối nội dung, hiện từ trên xuống
 * @param inputs các ô nhập, giá trị được gửi về [DialogAction.Callback] của nút
 * @param canCloseWithEscape cho phép đóng dialog bằng phím Esc
 * @param pause tạm dừng game khi chơi đơn, bị bỏ qua khi [afterAction] là [AfterAction.NONE]
 * @param afterAction việc client làm sau khi bấm nút
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

/**
 * Dialog thông báo, 1 nút duy nhất.
 *
 * @param title tiêu đề trên đầu dialog
 * @param action nút duy nhất, null là nút mặc định của client và không có action
 * @param body các khối nội dung, hiện từ trên xuống
 * @param inputs các ô nhập, giá trị được gửi về [DialogAction.Callback] của nút
 * @param externalTitle tên dùng cho nút dẫn tới dialog này, null thì dùng [title]
 * @param canCloseWithEscape cho phép đóng dialog bằng phím Esc
 * @param pause tạm dừng game khi chơi đơn, bị bỏ qua khi [afterAction] là [AfterAction.NONE]
 * @param afterAction việc client làm sau khi bấm nút
 */
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

/**
 * Dialog 2 nút có / không.
 *
 * @param title tiêu đề trên đầu dialog
 * @param yes nút đồng ý
 * @param no nút từ chối
 * @param body các khối nội dung, hiện từ trên xuống
 * @param inputs các ô nhập, giá trị được gửi về [DialogAction.Callback] của nút
 * @param externalTitle tên dùng cho nút dẫn tới dialog này, null thì dùng [title]
 * @param canCloseWithEscape cho phép đóng dialog bằng phím Esc
 * @param pause tạm dừng game khi chơi đơn, bị bỏ qua khi [afterAction] là [AfterAction.NONE]
 * @param afterAction việc client làm sau khi bấm nút
 */
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

/**
 * Dialog nhiều nút xếp theo cột.
 *
 * @param title tiêu đề trên đầu dialog
 * @param actions các nút, cần ít nhất 1 nút
 * @param columns số cột, phải > 0
 * @param exitAction nút ở chân dialog, null là không có
 * @param body các khối nội dung, hiện từ trên xuống
 * @param inputs các ô nhập, giá trị được gửi về [DialogAction.Callback] của nút
 * @param externalTitle tên dùng cho nút dẫn tới dialog này, null thì dùng [title]
 * @param canCloseWithEscape cho phép đóng dialog bằng phím Esc
 * @param pause tạm dừng game khi chơi đơn, bị bỏ qua khi [afterAction] là [AfterAction.NONE]
 * @param afterAction việc client làm sau khi bấm nút
 * @throws IllegalArgumentException nếu [actions] rỗng hoặc [columns] không hợp lệ
 */
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

/**
 * Việc client làm sau khi player bấm nút của dialog.
 *
 * @param id giá trị after_action trong format của dialog
 */
enum class AfterAction(val id: String) {
    /** Đóng dialog. */
    CLOSE("close"),

    /** Giữ dialog đang mở, nút callback bấm được nhiều lần. */
    NONE("none"),

    /** Hiện màn "Waiting for Server" tới khi có dialog mới hoặc [DialogAPI.clear]. */
    WAIT_FOR_RESPONSE("wait_for_response"),
}

/**
 * Nút của dialog.
 *
 * @param label chữ trên nút
 * @param action việc chạy khi bấm, null thì chỉ áp [Dialog.afterAction]
 * @param tooltip chữ hiện khi trỏ chuột vào nút, null là không có
 * @param width độ rộng của nút, null là mặc định của client
 */
class DialogButton(
    val label: Component,
    val action: DialogAction? = null,
    val tooltip: Component? = null,
    val width: Int? = null,
)

/** Khối nội dung của dialog. */
sealed class DialogBody {
    /**
     * Khối chữ dựng từ Component.
     *
     * @param contents nội dung
     * @param width độ rộng của khối chữ, null là mặc định của client
     */
    class PlainMessage(val contents: Component, val width: Int? = null) : DialogBody()

    /**
     * Như [PlainMessage] nhưng tự khai báo từng đoạn chữ, click và tooltip nằm ngay trong chữ.
     * Dùng khi Component không đủ: Adventure của server cũ chưa biết shadow color và click custom.
     *
     * @param runs các đoạn chữ, nối liền nhau theo thứ tự
     * @param width độ rộng của khối chữ, null là mặc định của client
     */
    class RichText(val runs: List<DialogTextRun>, val width: Int? = null) : DialogBody()
}

/**
 * Một đoạn chữ của [DialogBody.RichText].
 *
 * @param text nội dung đoạn chữ
 * @param font id font, null là font mặc định
 * @param color màu RGB trong khoảng 0..0xFFFFFF, null là mặc định
 * @param bold in đậm
 * @param shadow false là tắt bóng chữ
 * @param click [DialogAction.Callback] trong chữ nhận [DialogResponse.values] rỗng vì click chữ không gửi được input.
 *   Click chữ cũng áp [Dialog.afterAction] như nút
 * @param tooltip chữ hiện khi trỏ chuột vào đoạn chữ, null là không có
 * @throws IllegalArgumentException nếu [color] hoặc [font] không hợp lệ
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

/**
 * Ô nhập của dialog.
 *
 * @param key khoá để lấy giá trị trong [DialogResponse], chỉ gồm a-z, 0-9, _ và không trùng trong 1 dialog
 * @throws IllegalArgumentException nếu [key] không hợp lệ
 */
sealed class DialogInput(val key: String) {
    init {
        require(KEY_PATTERN.matches(key)) { "Input key không hợp lệ: $key (chỉ a-z, 0-9, _)" }
    }

    /**
     * Ô nhập chữ, mặc định 1 dòng.
     *
     * @param key khoá để lấy giá trị trong [DialogResponse]
     * @param label nhãn của ô nhập
     * @param initial giá trị ban đầu, không dài hơn [maxLength]
     * @param maxLength số ký tự tối đa, phải > 0
     * @param width độ rộng của ô nhập, null là mặc định của client
     * @param labelVisible hiện [label]
     * @param multiline cho nhập nhiều dòng, các dòng ngăn cách bằng `\n`
     * - Đường ViaVersion cần hook custom_click_action: ở đường lệnh ẩn, ký tự xuống dòng trong chat command
     *   sẽ bị server kick, nên [DialogAPI.show] ném [IllegalArgumentException]
     * - Packet callback tối đa 65536 byte, nên tránh đặt [maxLength] quá lớn
     * @throws IllegalArgumentException nếu [maxLength] hoặc [initial] không hợp lệ
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

        /**
         * Tuỳ chọn của ô nhập nhiều dòng.
         *
         * @param maxLines số dòng tối đa, phải > 0, null là không giới hạn
         * @param height chiều cao của ô nhập, trong khoảng 1..512, null là mặc định của client
         * @throws IllegalArgumentException nếu [maxLines] hoặc [height] không hợp lệ
         */
        class Multiline(val maxLines: Int? = null, val height: Int? = null) {
            // codec của client từ chối, ở đường ViaVersion player sẽ bị ngắt kết nối
            init {
                require(maxLines == null || maxLines > 0) { "maxLines phải > 0" }
                require(height == null || height in 1..512) { "height phải trong khoảng 1..512" }
            }
        }
    }

    /**
     * Ô đánh dấu.
     *
     * @param key khoá để lấy giá trị trong [DialogResponse]
     * @param label nhãn của ô
     * @param initial trạng thái ban đầu
     * @param onTrue giá trị trả về khi được đánh dấu
     * @param onFalse giá trị trả về khi không được đánh dấu
     */
    class Bool(
        key: String,
        val label: Component,
        val initial: Boolean = false,
        val onTrue: String = "true",
        val onFalse: String = "false",
    ) : DialogInput(key)

    /**
     * Ô chọn 1 trong nhiều giá trị.
     *
     * @param key khoá để lấy giá trị trong [DialogResponse]
     * @param label nhãn của ô
     * @param options các lựa chọn, cần ít nhất 1 lựa chọn
     * @param width độ rộng của ô, null là mặc định của client
     * @param labelVisible hiện [label]
     * @throws IllegalArgumentException nếu [options] rỗng
     */
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

        /**
         * Một lựa chọn của [SingleOption].
         *
         * @param id giá trị trả về khi được chọn
         * @param display chữ hiển thị, null là dùng [id]
         * @param initial được chọn sẵn
         */
        class Option(val id: String, val display: Component? = null, val initial: Boolean = false)
    }

    /**
     * Thanh trượt chọn số.
     *
     * @param key khoá để lấy giá trị trong [DialogResponse]
     * @param label nhãn của thanh trượt
     * @param start giá trị ở đầu thanh trượt
     * @param end giá trị ở cuối thanh trượt, được phép nhỏ hơn [start]
     * @param step bước nhảy, phải > 0, null là không chia bước
     * @param initial giá trị ban đầu, nằm giữa [start] và [end], null là mặc định của client
     * @param width độ rộng của thanh trượt, null là mặc định của client
     * @param labelFormat format của nhãn kèm giá trị, null là mặc định của client
     * @throws IllegalArgumentException nếu [step] hoặc [initial] không hợp lệ
     */
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
