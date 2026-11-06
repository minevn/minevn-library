import com.viaversion.nbt.tag.CompoundTag
import com.viaversion.nbt.tag.ListTag
import net.minevn.libs.bukkit.dialog.DialogAction
import net.minevn.libs.bukkit.dialog.DialogBody
import net.minevn.libs.bukkit.dialog.DialogButton
import net.minevn.libs.bukkit.dialog.DialogTextRun
import net.minevn.libs.bukkit.dialog.NoticeDialog
import net.minevn.libs.bukkit.dialog.via.DialogEncoder
import net.kyori.adventure.text.Component
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

class DialogRichTextTest {
    private fun encode(useCustomAction: Boolean, vararg runs: DialogTextRun): Pair<DialogEncoder, List<CompoundTag>> {
        val dialog = NoticeDialog(
            title = Component.empty(),
            action = DialogButton(Component.empty(), width = 2),
            body = listOf(DialogBody.RichText(runs.toList(), 400)),
        )
        val encoder = DialogEncoder(dialog, "tokenvalue", "~s~", useCustomAction)
        val body = encoder.encode().getListTag("body", CompoundTag::class.java)!![0]
        assertEquals("minecraft:plain_message", body.getString("type"))
        assertEquals(400, body.getInt("width"))
        val extra = body.getCompoundTag("contents")!!.get("extra") as ListTag<*>
        return encoder to extra.value.map { it as CompoundTag }
    }

    @Test
    fun testStyleIsWritten() {
        val (_, tags) = encode(true, DialogTextRun("a", "x:y", 0x12AB34, bold = true), DialogTextRun("b", shadow = true))
        assertEquals("x:y", tags[0].getString("font"))
        assertEquals("#12ab34", tags[0].getString("color"))
        assertEquals(0, tags[0].getInt("shadow_color"))
        assertFalse(tags[1].contains("font"))
        assertFalse(tags[1].contains("shadow_color"))
    }

    @Test
    fun testCallbackSharedAcrossRunsRegistersOnce() {
        val callback = DialogAction.Callback { }
        val (encoder, tags) = encode(true, DialogTextRun("a", click = callback), DialogTextRun("b", click = callback))
        assertEquals(1, encoder.handlers.size)
        for (tag in tags) {
            val click = tag.getCompoundTag("click_event")!!
            assertEquals("custom", click.getString("action"))
            assertEquals("minevnlib:cb/tokenvalue/0", click.getString("id"))
        }
    }

    @Test
    fun testCallbackFallsBackToCommandWithoutHook() {
        val (_, tags) = encode(false, DialogTextRun("a", click = DialogAction.Callback { }))
        val click = tags[0].getCompoundTag("click_event")!!
        assertEquals("run_command", click.getString("action"))
        assertEquals("/${DialogEncoder.CALLBACK_COMMAND} tokenvalue.0", click.getString("command"))
    }

    @Test
    fun testTooltipIsHoverEvent() {
        val (_, tags) = encode(true, DialogTextRun("a", tooltip = Component.text("hi")))
        assertEquals("show_text", tags[0].getCompoundTag("hover_event")!!.getString("action"))
    }
}
