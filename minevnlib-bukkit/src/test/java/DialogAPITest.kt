import net.minevn.libs.bukkit.dialog.DialogAPI
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DialogAPITest {
    @Test
    fun testIsAtLeast1216() {
        assertFalse(DialogAPI.isAtLeast1216("1.8.8-R0.1-SNAPSHOT"))
        assertFalse(DialogAPI.isAtLeast1216("1.20.6-R0.1-SNAPSHOT"))
        assertFalse(DialogAPI.isAtLeast1216("1.21-R0.1-SNAPSHOT"))
        assertFalse(DialogAPI.isAtLeast1216("1.21.5-R0.1-SNAPSHOT"))
        assertTrue(DialogAPI.isAtLeast1216("1.21.6-R0.1-SNAPSHOT"))
        assertTrue(DialogAPI.isAtLeast1216("1.21.11-R0.1-SNAPSHOT"))
        assertTrue(DialogAPI.isAtLeast1216("26.3-R0.1-SNAPSHOT"))
    }
}
