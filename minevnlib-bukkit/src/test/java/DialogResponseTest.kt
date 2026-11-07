import net.kyori.adventure.text.Component
import net.minevn.libs.bukkit.dialog.DialogInput
import net.minevn.libs.bukkit.dialog.DialogResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class DialogResponseTest {
    private val label = Component.text("label")

    @Test
    fun testSanitizeText() {
        val input = DialogInput.Text("name", label, maxLength = 4)
        assertEquals("abc", DialogResponse.sanitize(input, "abc"))
        assertEquals("abcd", DialogResponse.sanitize(input, "abcdef"))
    }

    @Test
    fun testSanitizeBool() {
        val input = DialogInput.Bool("silent", label, onTrue = "on", onFalse = "off")
        assertEquals("on", DialogResponse.sanitize(input, "on"))
        assertEquals("off", DialogResponse.sanitize(input, "off"))
        assertNull(DialogResponse.sanitize(input, "true"))
    }

    @Test
    fun testSanitizeSingleOption() {
        val input = DialogInput.SingleOption("mode", label, listOf(
            DialogInput.SingleOption.Option("easy"),
            DialogInput.SingleOption.Option("hard"),
        ))
        assertEquals("hard", DialogResponse.sanitize(input, "hard"))
        assertNull(DialogResponse.sanitize(input, "god"))
    }

    @Test
    fun testSanitizeNumberRange() {
        val input = DialogInput.NumberRange("amount", label, 1f, 64f)
        assertEquals("16", DialogResponse.sanitize(input, "16.0"))
        assertEquals("2.5", DialogResponse.sanitize(input, "2.5"))
        assertEquals("64", DialogResponse.sanitize(input, "1000"))
        assertEquals("1", DialogResponse.sanitize(input, "-5"))
        assertNull(DialogResponse.sanitize(input, "NaN"))
        assertNull(DialogResponse.sanitize(input, "abc"))

        val reversed = DialogInput.NumberRange("amount", label, 10f, 0f)
        assertEquals("10", DialogResponse.sanitize(reversed, "20"))
    }

    @Test
    fun testSanitizeAll() {
        val inputs = listOf(
            DialogInput.Text("name", label, maxLength = 4),
            DialogInput.Bool("silent", label),
        )
        assertEquals(
            mapOf("name" to "abcd", "silent" to "true"),
            DialogResponse.sanitizeAll(inputs) { if (it.key == "name") "abcdef" else "true" },
        )
        // thiếu hoặc sai 1 input thì bỏ cả click
        assertNull(DialogResponse.sanitizeAll(inputs) { if (it.key == "name") "abc" else null })
        assertNull(DialogResponse.sanitizeAll(inputs) { if (it.key == "name") "abc" else "yes" })
    }
}
