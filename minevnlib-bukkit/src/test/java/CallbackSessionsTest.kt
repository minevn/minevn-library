import io.mockk.every
import io.mockk.mockk
import net.kyori.adventure.text.Component
import net.minevn.libs.bukkit.dialog.AfterAction
import net.minevn.libs.bukkit.dialog.DialogClickGuard
import net.minevn.libs.bukkit.dialog.DialogInput
import net.minevn.libs.bukkit.dialog.DialogResponse
import net.minevn.libs.bukkit.dialog.via.CallbackSessions
import net.minevn.libs.bukkit.dialog.via.DialogEncoder
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.util.UUID
import java.util.logging.Logger

class CallbackSessionsTest {
    private val player = mockk<Player>(relaxed = true).also { every { it.uniqueId } returns UUID.randomUUID() }
    private val sessions = CallbackSessions(Logger.getLogger("test"), DialogClickGuard())
    private val responses = mutableListOf<DialogResponse>()

    private fun command(message: String) {
        val event = mockk<PlayerCommandPreprocessEvent>(relaxed = true)
        every { event.message } returns message
        every { event.player } returns player
        sessions.onCommand(event)
    }

    private fun putSession(afterAction: AfterAction) = sessions.put(player, CallbackSessions.Session(
        "tok", "~s~", listOf(DialogInput.Text("name", Component.text("label"))),
        listOf({ response: DialogResponse -> responses += response }, { response -> responses += response }),
        afterAction,
        textHandlers = setOf(0),
    ))

    @Test
    fun testTextClickRunsWithoutInputs() {
        putSession(AfterAction.NONE)
        // click chữ không gửi được giá trị input
        command("/${DialogEncoder.CALLBACK_COMMAND} tok.0")
        assertEquals(emptyMap<String, String>(), responses.single().values)

        // nút vẫn phải gửi đủ input
        command("/${DialogEncoder.CALLBACK_COMMAND} tok.1")
        assertEquals(1, responses.size)
        command("/${DialogEncoder.CALLBACK_COMMAND} tok.1 ~s~abc")
        assertEquals("abc", responses.last().string("name"))
    }

    @Test
    fun testTextClickCountsAsOneClick() {
        putSession(AfterAction.CLOSE)
        command("/${DialogEncoder.CALLBACK_COMMAND} tok.0")
        // dialog đóng sau click chữ thì nút của cùng dialog bị bỏ
        command("/${DialogEncoder.CALLBACK_COMMAND} tok.1 ~s~abc")
        assertEquals(1, responses.size)
        assertNull(responses.single().string("name"))
    }
}
