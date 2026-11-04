package net.minevn.libs.paper.dialog

import io.papermc.paper.dialog.DialogResponseView
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.event.ClickCallback
import net.kyori.adventure.text.event.ClickEvent
import net.minevn.libs.bukkit.MineVNLib
import net.minevn.libs.bukkit.dialog.AfterAction
import net.minevn.libs.bukkit.dialog.ConfirmationDialog
import net.minevn.libs.bukkit.dialog.Dialog
import net.minevn.libs.bukkit.dialog.DialogAction
import net.minevn.libs.bukkit.dialog.DialogBackend
import net.minevn.libs.bukkit.dialog.DialogBody
import net.minevn.libs.bukkit.dialog.DialogButton
import net.minevn.libs.bukkit.dialog.DialogInput
import net.minevn.libs.bukkit.dialog.DialogResponse
import net.minevn.libs.bukkit.dialog.MultiActionDialog
import net.minevn.libs.bukkit.dialog.NoticeDialog
import net.minevn.libs.bukkit.getClientProtocolVersion
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.logging.Level
import io.papermc.paper.dialog.Dialog as PaperDialog
import io.papermc.paper.registry.data.dialog.action.DialogAction as PaperDialogAction
import io.papermc.paper.registry.data.dialog.body.DialogBody as PaperDialogBody
import io.papermc.paper.registry.data.dialog.input.DialogInput as PaperDialogInput

/**
 * Gửi dialog bằng dialog API của Paper (1.21.7+), đổi model của MineVNLib sang model của Paper.
 * DialogAPI nạp class này qua reflection, chỉ khi server có dialog API của Paper.
 */
class PaperDialogBackend : DialogBackend {
    // client cũ hơn 1.21.6 vào qua ViaBackwards không hiện được dialog gốc
    override fun isSupported(player: Player) = !Bukkit.getPluginManager().isPluginEnabled("ViaVersion") ||
        player.getClientProtocolVersion() >= PROTOCOL_1_21_6

    override fun show(player: Player, dialog: Dialog) {
        val base = DialogBase.builder(dialog.title)
            .externalTitle(dialog.externalTitle)
            .canCloseWithEscape(dialog.canCloseWithEscape)
            // client không cho pause khi after_action là none
            .pause(dialog.pause && dialog.afterAction != AfterAction.NONE)
            .afterAction(DialogBase.DialogAfterAction.valueOf(dialog.afterAction.name))
            .body(dialog.body.map(::toPaperBody))
            .inputs(dialog.inputs.map(::toPaperInput))
            .build()
        val type = when (dialog) {
            is NoticeDialog -> dialog.action
                ?.let { DialogType.notice(toPaperButton(it, dialog)) }
                ?: DialogType.notice()
            is ConfirmationDialog -> DialogType.confirmation(toPaperButton(dialog.yes, dialog), toPaperButton(dialog.no, dialog))
            is MultiActionDialog -> DialogType.multiAction(dialog.actions.map { toPaperButton(it, dialog) })
                .exitAction(dialog.exitAction?.let { toPaperButton(it, dialog) })
                .columns(dialog.columns)
                .build()
        }
        player.showDialog(PaperDialog.create { factory -> factory.empty().base(base).type(type) })
    }

    // Audience#closeDialog có từ Adventure 4.24 (Paper 1.21.8)
    override fun clear(player: Player) {
        player.closeDialog()
    }

    private fun toPaperBody(body: DialogBody) = when (body) {
        is DialogBody.PlainMessage -> body.width
            ?.let { PaperDialogBody.plainMessage(body.contents, it) }
            ?: PaperDialogBody.plainMessage(body.contents)
    }

    private fun toPaperInput(input: DialogInput): PaperDialogInput = when (input) {
        is DialogInput.Text -> PaperDialogInput.text(input.key, input.label)
            .labelVisible(input.labelVisible)
            .initial(input.initial)
            .maxLength(input.maxLength)
            .apply { input.width?.let { width(it) } }
            .build()
        is DialogInput.Bool -> PaperDialogInput.bool(input.key, input.label)
            .initial(input.initial)
            .onTrue(input.onTrue)
            .onFalse(input.onFalse)
            .build()
        is DialogInput.SingleOption -> PaperDialogInput
            .singleOption(input.key, input.label, input.options.map { option ->
                SingleOptionDialogInput.OptionEntry.create(option.id, option.display, option.initial)
            })
            .labelVisible(input.labelVisible)
            .apply { input.width?.let { width(it) } }
            .build()
        is DialogInput.NumberRange -> PaperDialogInput.numberRange(input.key, input.label, input.start, input.end)
            .step(input.step)
            .initial(input.initial)
            .apply {
                input.width?.let { width(it) }
                input.labelFormat?.let { labelFormat(it) }
            }
            .build()
    }

    private fun toPaperButton(button: DialogButton, dialog: Dialog) = ActionButton.builder(button.label)
        .tooltip(button.tooltip)
        .action(button.action?.let { toPaperAction(it, dialog) })
        .apply { button.width?.let { width(it) } }
        .build()

    private fun toPaperAction(action: DialogAction, dialog: Dialog): PaperDialogAction = when (action) {
        is DialogAction.OpenUrl -> PaperDialogAction.staticAction(ClickEvent.openUrl(action.url))
        is DialogAction.RunCommand -> PaperDialogAction.staticAction(ClickEvent.runCommand(action.command))
        is DialogAction.SuggestCommand -> PaperDialogAction.staticAction(ClickEvent.suggestCommand(action.command))
        is DialogAction.CopyToClipboard -> PaperDialogAction.staticAction(ClickEvent.copyToClipboard(action.value))
        is DialogAction.Callback -> {
            // như đường ViaVersion: dialog đóng sau khi bấm thì callback chỉ chạy 1 lần
            val uses = if (dialog.afterAction == AfterAction.NONE) ClickCallback.UNLIMITED_USES else 1
            PaperDialogAction.customClick(
                { response, audience -> onCallback(action, dialog, response, audience) },
                ClickCallback.Options.builder().uses(uses).build(),
            )
        }
    }

    private fun onCallback(action: DialogAction.Callback, dialog: Dialog, response: DialogResponseView, audience: Audience) {
        val player = audience as? Player ?: return
        val values = dialog.inputs
            .mapNotNull { input -> responseValue(input, response)?.let { input.key to it } }
            .toMap()
        try {
            action.handler(DialogResponse(player, values))
        } catch (exception: Exception) {
            MineVNLib.instance.logger.log(Level.SEVERE, "Lỗi khi xử lý callback dialog của ${player.name}", exception)
        }
    }

    /** Trả giá trị dạng chuỗi giống đường ViaVersion: boolean theo on_true / on_false, số bỏ phần .0 */
    private fun responseValue(input: DialogInput, response: DialogResponseView): String? = when (input) {
        is DialogInput.Text -> response.getText(input.key)
        is DialogInput.SingleOption -> response.getText(input.key)
        is DialogInput.Bool -> response.getBoolean(input.key)?.let { checked -> if (checked) input.onTrue else input.onFalse }
        is DialogInput.NumberRange -> response.getFloat(input.key)?.let { DialogResponse.formatNumber(it) }
    }

    companion object {
        /** Protocol version của Minecraft 1.21.6 */
        private const val PROTOCOL_1_21_6 = 771
    }
}
