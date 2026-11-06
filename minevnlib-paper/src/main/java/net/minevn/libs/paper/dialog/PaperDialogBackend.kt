package net.minevn.libs.paper.dialog

import io.papermc.paper.dialog.DialogResponseView
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickCallback
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.ShadowColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.minevn.libs.bukkit.MineVNLib
import net.minevn.libs.bukkit.dialog.AfterAction
import net.minevn.libs.bukkit.dialog.ConfirmationDialog
import net.minevn.libs.bukkit.dialog.Dialog
import net.minevn.libs.bukkit.dialog.DialogAction
import net.minevn.libs.bukkit.dialog.DialogBackend
import net.minevn.libs.bukkit.dialog.DialogBody
import net.minevn.libs.bukkit.dialog.DialogButton
import net.minevn.libs.bukkit.dialog.DialogClickGuard
import net.minevn.libs.bukkit.dialog.DialogInput
import net.minevn.libs.bukkit.dialog.DialogResponse
import net.minevn.libs.bukkit.dialog.DialogTextRun
import net.minevn.libs.bukkit.dialog.MultiActionDialog
import net.minevn.libs.bukkit.dialog.NoticeDialog
import net.minevn.libs.bukkit.getClientProtocolVersion
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Level
import io.papermc.paper.dialog.Dialog as PaperDialog
import io.papermc.paper.registry.data.dialog.action.DialogAction as PaperDialogAction
import io.papermc.paper.registry.data.dialog.body.DialogBody as PaperDialogBody
import io.papermc.paper.registry.data.dialog.input.DialogInput as PaperDialogInput

/**
 * Gửi dialog bằng dialog API của Paper (1.21.7+), đổi model của MineVNLib sang model của Paper.
 * DialogAPI nạp class này qua reflection, chỉ khi server có dialog API của Paper.
 *
 * Callback của Paper sống tới hết lifetime dù player đã được mở dialog khác,
 * nên backend tự nhớ lần mở mới nhất của mỗi player, giống đường ViaVersion.
 */
class PaperDialogBackend : DialogBackend, Listener {
    /** Mỗi lần mở dialog là 1 object, callback của lần mở khác bị bỏ qua */
    private class Showing

    private val guard = DialogClickGuard()
    private val showings = ConcurrentHashMap<UUID, Showing>()

    init {
        val plugin = MineVNLib.instance
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    // client cũ hơn 1.21.6 vào qua ViaBackwards không hiện được dialog gốc
    override fun isSupported(player: Player) = !Bukkit.getPluginManager().isPluginEnabled("ViaVersion") ||
        player.getClientProtocolVersion() >= PROTOCOL_1_21_6

    override fun show(player: Player, dialog: Dialog) {
        val showing = Showing()
        val base = DialogBase.builder(dialog.title)
            .externalTitle(dialog.externalTitle)
            .canCloseWithEscape(dialog.canCloseWithEscape)
            // client không cho pause khi after_action là none
            .pause(dialog.pause && dialog.afterAction != AfterAction.NONE)
            .afterAction(DialogBase.DialogAfterAction.valueOf(dialog.afterAction.name))
            .body(dialog.body.map { toPaperBody(it, dialog, showing) })
            .inputs(dialog.inputs.map(::toPaperInput))
            .build()
        val type = when (dialog) {
            is NoticeDialog -> dialog.action
                ?.let { DialogType.notice(toPaperButton(it, dialog, showing)) }
                ?: DialogType.notice()
            is ConfirmationDialog -> DialogType.confirmation(
                toPaperButton(dialog.yes, dialog, showing),
                toPaperButton(dialog.no, dialog, showing),
            )
            is MultiActionDialog -> DialogType.multiAction(dialog.actions.map { toPaperButton(it, dialog, showing) })
                .exitAction(dialog.exitAction?.let { toPaperButton(it, dialog, showing) })
                .columns(dialog.columns)
                .build()
        }
        showings[player.uniqueId] = showing
        guard.onShow(player)
        player.showDialog(PaperDialog.create { factory -> factory.empty().base(base).type(type) })
    }

    // Audience#closeDialog có từ Adventure 4.24 (Paper 1.21.8)
    override fun clear(player: Player) {
        showings.remove(player.uniqueId)
        player.closeDialog()
        guard.closeWaitingScreen(player)
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        showings.remove(event.player.uniqueId)
        guard.forget(event.player.uniqueId)
    }

    private fun toPaperBody(body: DialogBody, dialog: Dialog, showing: Showing): PaperDialogBody = when (body) {
        is DialogBody.PlainMessage -> body.width
            ?.let { PaperDialogBody.plainMessage(body.contents, it) }
            ?: PaperDialogBody.plainMessage(body.contents)
        is DialogBody.RichText -> {
            val contents = Component.empty().children(body.runs.map { toPaperRun(it, dialog, showing) })
            body.width
                ?.let { PaperDialogBody.plainMessage(contents, it) }
                ?: PaperDialogBody.plainMessage(contents)
        }
    }

    private fun toPaperRun(run: DialogTextRun, dialog: Dialog, showing: Showing): Component {
        var component: Component = Component.text(run.text)
        run.font?.let { component = component.font(Key.key(it)) }
        run.color?.let { component = component.color(TextColor.color(it)) }
        if (run.bold) {
            component = component.decorate(TextDecoration.BOLD)
        }
        if (!run.shadow) {
            component = component.shadowColor(ShadowColor.none())
        }
        run.click?.let { component = component.clickEvent(toPaperClick(it, dialog, showing)) }
        run.tooltip?.let { component = component.hoverEvent(HoverEvent.showText(it)) }
        return component
    }

    private fun toPaperClick(action: DialogAction, dialog: Dialog, showing: Showing): ClickEvent = when (action) {
        is DialogAction.OpenUrl -> ClickEvent.openUrl(action.url)
        is DialogAction.RunCommand -> ClickEvent.runCommand(action.command)
        is DialogAction.SuggestCommand -> ClickEvent.suggestCommand(action.command)
        is DialogAction.CopyToClipboard -> ClickEvent.copyToClipboard(action.value)
        is DialogAction.Callback -> ClickEvent.callback(
            { audience -> onTextClick(action, dialog, showing, audience) },
            ClickCallback.Options.builder().uses(ClickCallback.UNLIMITED_USES).build(),
        )
    }

    /** Click trong chữ không gửi kèm giá trị input như nút. */
    private fun onTextClick(action: DialogAction.Callback, dialog: Dialog, showing: Showing, audience: Audience) {
        val player = audience as? Player ?: return
        if (!guard.allowClick(player.uniqueId) || showings[player.uniqueId] !== showing) {
            return
        }
        guard.onClick(player, dialog.afterAction)
        try {
            action.handler(DialogResponse(player, emptyMap()))
        } catch (exception: Exception) {
            MineVNLib.instance.logger.log(Level.SEVERE, "Lỗi khi xử lý callback dialog của ${player.name}", exception)
            guard.closeWaitingScreen(player)
        }
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

    private fun toPaperButton(button: DialogButton, dialog: Dialog, showing: Showing): ActionButton =
        ActionButton.builder(button.label)
            .tooltip(button.tooltip)
            .action(button.action?.let { toPaperAction(it, dialog, showing) })
            .apply { button.width?.let { width(it) } }
            .build()

    private fun toPaperAction(action: DialogAction, dialog: Dialog, showing: Showing): PaperDialogAction = when (action) {
        is DialogAction.OpenUrl -> PaperDialogAction.staticAction(ClickEvent.openUrl(action.url))
        is DialogAction.RunCommand -> PaperDialogAction.staticAction(ClickEvent.runCommand(action.command))
        is DialogAction.SuggestCommand -> PaperDialogAction.staticAction(ClickEvent.suggestCommand(action.command))
        is DialogAction.CopyToClipboard -> PaperDialogAction.staticAction(ClickEvent.copyToClipboard(action.value))
        is DialogAction.Callback -> {
            // như đường ViaVersion: dialog đóng sau khi bấm thì callback chỉ chạy 1 lần
            val uses = if (dialog.afterAction == AfterAction.NONE) ClickCallback.UNLIMITED_USES else 1
            PaperDialogAction.customClick(
                { response, audience -> onCallback(action, dialog, showing, response, audience) },
                ClickCallback.Options.builder().uses(uses).build(),
            )
        }
    }

    private fun onCallback(
        action: DialogAction.Callback,
        dialog: Dialog,
        showing: Showing,
        response: DialogResponseView,
        audience: Audience,
    ) {
        val player = audience as? Player ?: return
        if (!guard.allowClick(player.uniqueId) || showings[player.uniqueId] !== showing) {
            return
        }
        val values = DialogResponse.sanitizeAll(dialog.inputs) { responseValue(it, response) } ?: return
        // như đường ViaVersion: dialog đóng sau khi bấm thì chỉ nhận 1 click, kể cả của nút khác
        if (dialog.afterAction != AfterAction.NONE && !showings.remove(player.uniqueId, showing)) {
            return
        }
        guard.onClick(player, dialog.afterAction)

        try {
            action.handler(DialogResponse(player, values))
        } catch (exception: Exception) {
            MineVNLib.instance.logger.log(Level.SEVERE, "Lỗi khi xử lý callback dialog của ${player.name}", exception)
            guard.closeWaitingScreen(player)
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
