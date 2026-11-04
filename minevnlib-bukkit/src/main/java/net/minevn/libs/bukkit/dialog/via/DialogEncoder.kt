package net.minevn.libs.bukkit.dialog.via

import com.viaversion.nbt.tag.CompoundTag
import com.viaversion.nbt.tag.ListTag
import net.kyori.adventure.text.Component
import net.minevn.libs.bukkit.dialog.AfterAction
import net.minevn.libs.bukkit.dialog.ConfirmationDialog
import net.minevn.libs.bukkit.dialog.Dialog
import net.minevn.libs.bukkit.dialog.DialogAction
import net.minevn.libs.bukkit.dialog.DialogBody
import net.minevn.libs.bukkit.dialog.DialogButton
import net.minevn.libs.bukkit.dialog.DialogInput
import net.minevn.libs.bukkit.dialog.DialogResponse
import net.minevn.libs.bukkit.dialog.MultiActionDialog
import net.minevn.libs.bukkit.dialog.NoticeDialog

/**
 * Encode [Dialog] thành NBT inline của packet show_dialog (1.21.6).
 * Mỗi [DialogAction.Callback] được gán 1 index trong [handlers], client gửi về bằng:
 * - [useCustomAction] = true: action custom / dynamic/custom, id `minevnlib:cb/<token>/<index>`,
 *   payload là compound giá trị input (bắt qua [ViaClickHook])
 * - [useCustomAction] = false: lệnh `/<CALLBACK_COMMAND> <token>.<index> <sep>v1<sep>v2...` theo thứ tự [keys]
 */
internal class DialogEncoder(
    private val dialog: Dialog,
    private val token: String,
    private val separator: String,
    private val useCustomAction: Boolean,
) {
    val keys: List<String> = dialog.inputs.map { it.key }
    val booleans: Map<String, Pair<String, String>> = dialog.inputs
        .filterIsInstance<DialogInput.Bool>()
        .associate { it.key to (it.onTrue to it.onFalse) }
    val handlers = mutableListOf<(DialogResponse) -> Unit>()

    init {
        require(keys.toSet().size == keys.size) { "Input key bị trùng: $keys" }
    }

    fun encode(): CompoundTag {
        val tag = CompoundTag()
        tag.putString("type", typeId())
        tag.put("title", ComponentNbt.toNbt(dialog.title))
        dialog.externalTitle?.let { tag.put("external_title", ComponentNbt.toNbt(it)) }
        tag.put("body", compoundList(dialog.body.map(::encodeBody)))
        tag.put("inputs", compoundList(dialog.inputs.map(::encodeInput)))
        tag.putBoolean("can_close_with_escape", dialog.canCloseWithEscape)
        // client không cho pause khi after_action là none
        tag.putBoolean("pause", dialog.pause && dialog.afterAction != AfterAction.NONE)
        tag.putString("after_action", dialog.afterAction.id)

        when (dialog) {
            is NoticeDialog -> dialog.action?.let { tag.put("action", encodeButton(it)) }
            is ConfirmationDialog -> {
                tag.put("yes", encodeButton(dialog.yes))
                tag.put("no", encodeButton(dialog.no))
            }
            is MultiActionDialog -> {
                tag.put("actions", compoundList(dialog.actions.map(::encodeButton)))
                tag.putInt("columns", dialog.columns)
                dialog.exitAction?.let { tag.put("exit_action", encodeButton(it)) }
            }
        }

        if (handlers.isNotEmpty() && !useCustomAction) {
            checkCommandLength()
        }
        return tag
    }

    private fun typeId() = when (dialog) {
        is NoticeDialog -> "minecraft:notice"
        is ConfirmationDialog -> "minecraft:confirmation"
        is MultiActionDialog -> "minecraft:multi_action"
    }

    private fun encodeBody(body: DialogBody) = CompoundTag().apply {
        when (body) {
            is DialogBody.PlainMessage -> {
                putString("type", "minecraft:plain_message")
                put("contents", ComponentNbt.toNbt(body.contents))
                body.width?.let { putInt("width", it) }
            }
        }
    }

    private fun encodeInput(input: DialogInput) = CompoundTag().apply {
        putString("key", input.key)
        when (input) {
            is DialogInput.Text -> {
                putString("type", "minecraft:text")
                putLabel(input.label)
                putBoolean("label_visible", input.labelVisible)
                putString("initial", input.initial)
                putInt("max_length", input.maxLength)
                input.width?.let { putInt("width", it) }
            }
            is DialogInput.Bool -> {
                putString("type", "minecraft:boolean")
                putLabel(input.label)
                putBoolean("initial", input.initial)
                putString("on_true", input.onTrue)
                putString("on_false", input.onFalse)
            }
            is DialogInput.SingleOption -> {
                putString("type", "minecraft:single_option")
                putLabel(input.label)
                putBoolean("label_visible", input.labelVisible)
                input.width?.let { putInt("width", it) }
                put("options", compoundList(input.options.map { option ->
                    CompoundTag().apply {
                        putString("id", option.id)
                        option.display?.let { put("display", ComponentNbt.toNbt(it)) }
                        if (option.initial) {
                            putBoolean("initial", true)
                        }
                    }
                }))
            }
            is DialogInput.NumberRange -> {
                putString("type", "minecraft:number_range")
                putLabel(input.label)
                putFloat("start", input.start)
                putFloat("end", input.end)
                input.step?.let { putFloat("step", it) }
                input.initial?.let { putFloat("initial", it) }
                input.width?.let { putInt("width", it) }
                input.labelFormat?.let { putString("label_format", it) }
            }
        }
    }

    private fun encodeButton(button: DialogButton) = CompoundTag().apply {
        putLabel(button.label)
        button.tooltip?.let { put("tooltip", ComponentNbt.toNbt(it)) }
        button.width?.let { putInt("width", it) }
        button.action?.let { put("action", encodeAction(it)) }
    }

    private fun encodeAction(action: DialogAction) = CompoundTag().apply {
        when (action) {
            is DialogAction.OpenUrl -> {
                putString("type", "open_url")
                putString("url", action.url)
            }
            is DialogAction.RunCommand -> {
                putString("type", "run_command")
                putString("command", action.command)
            }
            is DialogAction.SuggestCommand -> {
                putString("type", "suggest_command")
                putString("command", action.command)
            }
            is DialogAction.CopyToClipboard -> {
                putString("type", "copy_to_clipboard")
                putString("value", action.value)
            }
            is DialogAction.Callback -> if (useCustomAction) {
                val id = "${ViaClickHook.ID_PREFIX}$token/${handlers.size}"
                handlers += action.handler
                // dynamic/custom tự gom giá trị toàn bộ input vào payload
                putString("type", if (keys.isEmpty()) "custom" else "dynamic/custom")
                putString("id", id)
            } else {
                val prefix = "/$CALLBACK_COMMAND $token.${handlers.size}"
                handlers += action.handler
                if (keys.isEmpty()) {
                    putString("type", "run_command")
                    putString("command", prefix)
                } else {
                    putString("type", "dynamic/run_command")
                    putString("template", prefix + " " + keys.joinToString("") { "$separator$(${it})" })
                }
            }
        }
    }

    /**
     * Server cũ hơn 1.21.6 đọc chat command tối đa 256 ký tự, quá là lỗi decode và player bị kick.
     * Ước lượng độ dài tối đa sau khi client thay macro, chặn từ lúc gửi.
     */
    private fun checkCommandLength() {
        val prefix = "/$CALLBACK_COMMAND $token.${handlers.size - 1} ".length
        val values = dialog.inputs.sumOf { input ->
            separator.length + when (input) {
                is DialogInput.Text -> input.maxLength
                is DialogInput.Bool -> maxOf(input.onTrue.length, input.onFalse.length)
                is DialogInput.SingleOption -> input.options.maxOf { it.id.length }
                is DialogInput.NumberRange -> NUMBER_MAX_LENGTH
            }
        }
        val max = prefix + values
        require(max <= MAX_COMMAND_LENGTH) {
            "Lệnh callback có thể dài tới $max ký tự (> $MAX_COMMAND_LENGTH), hãy giảm max_length hoặc số input"
        }
    }

    private fun CompoundTag.putLabel(label: Component) = put("label", ComponentNbt.toNbt(label))

    private fun compoundList(tags: List<CompoundTag>) = ListTag(CompoundTag::class.java).apply { tags.forEach(::add) }

    companion object {
        /** Lệnh ẩn nhận callback khi không hook được custom_click_action, khai báo trong plugin.yml */
        const val CALLBACK_COMMAND = "mvndialogcb"
        private const val MAX_COMMAND_LENGTH = 256
        private const val NUMBER_MAX_LENGTH = 16
    }
}
