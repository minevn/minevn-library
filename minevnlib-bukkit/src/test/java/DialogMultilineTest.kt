import com.viaversion.nbt.tag.CompoundTag
import net.kyori.adventure.text.Component
import net.minevn.libs.bukkit.dialog.DialogAction
import net.minevn.libs.bukkit.dialog.DialogButton
import net.minevn.libs.bukkit.dialog.DialogInput
import net.minevn.libs.bukkit.dialog.DialogResponse
import net.minevn.libs.bukkit.dialog.NoticeDialog
import net.minevn.libs.bukkit.dialog.via.DialogEncoder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class DialogMultilineTest {
    private val label = Component.text("label")

    private fun encoder(input: DialogInput.Text, useCustomAction: Boolean) = DialogEncoder(
        NoticeDialog(
            title = Component.empty(),
            inputs = listOf(input),
            action = DialogButton(Component.empty(), DialogAction.Callback { }),
        ),
        "tokenvalue",
        "~s~",
        useCustomAction,
    )

    private fun encodedInput(input: DialogInput.Text) =
        encoder(input, true).encode().getListTag("inputs", CompoundTag::class.java)!![0]

    @Test
    fun testEncodeMultiline() {
        val tag = encodedInput(DialogInput.Text("note", label, multiline = DialogInput.Text.Multiline(5, 100)))
        val multiline = tag.getCompoundTag("multiline")!!
        assertEquals(5, multiline.getInt("max_lines"))
        assertEquals(100, multiline.getInt("height"))
    }

    @Test
    fun testEncodeOmitsUnsetFields() {
        val tag = encodedInput(DialogInput.Text("note", label, multiline = DialogInput.Text.Multiline()))
        assertFalse(tag.getCompoundTag("multiline")!!.contains("max_lines"))
        assertFalse(tag.getCompoundTag("multiline")!!.contains("height"))
        assertFalse(encodedInput(DialogInput.Text("note", label)).contains("multiline"))
    }

    @Test
    fun testCommandRouteRejectsMultiline() {
        val input = DialogInput.Text("note", label, multiline = DialogInput.Text.Multiline())
        assertThrows(IllegalArgumentException::class.java) { encoder(input, false).encode() }
    }

    @Test
    fun testInvalidOptionsRejected() {
        assertThrows(IllegalArgumentException::class.java) { DialogInput.Text.Multiline(maxLines = 0) }
        assertThrows(IllegalArgumentException::class.java) { DialogInput.Text.Multiline(height = 0) }
        assertThrows(IllegalArgumentException::class.java) { DialogInput.Text.Multiline(height = 513) }
    }

    @Test
    fun testSanitizeLines() {
        val singleLine = DialogInput.Text("note", label, maxLength = 20)
        assertEquals("a", DialogResponse.sanitize(singleLine, "a\nb"))

        val limited = DialogInput.Text("note", label, maxLength = 20, multiline = DialogInput.Text.Multiline(2))
        assertEquals("a\nb", DialogResponse.sanitize(limited, "a\nb\nc"))
        assertEquals("a\nb", DialogResponse.sanitize(limited, "a\r\nb"))

        val unlimited = DialogInput.Text("note", label, maxLength = 3, multiline = DialogInput.Text.Multiline())
        assertEquals("a\nb", DialogResponse.sanitize(unlimited, "a\nb\nc"))
    }
}
