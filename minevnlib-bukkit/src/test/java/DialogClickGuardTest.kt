import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.minevn.libs.bukkit.dialog.AfterAction
import net.minevn.libs.bukkit.dialog.DialogClickGuard
import net.minevn.libs.bukkit.dialog.DialogResponse
import net.minevn.libs.bukkit.dialog.via.CallbackSessions
import net.minevn.libs.bukkit.dialog.via.DialogEncoder
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

class DialogClickGuardTest {
    @Test
    fun testAllowClick() {
        var now = 5_000L
        val guard = DialogClickGuard { now }
        val uuid = UUID.randomUUID()

        repeat(DialogClickGuard.MAX_CLICKS_PER_SECOND) { assertTrue(guard.allowClick(uuid)) }
        assertFalse(guard.allowClick(uuid))
        // player khác không bị ảnh hưởng
        assertTrue(guard.allowClick(UUID.randomUUID()))

        now += 1_000
        assertTrue(guard.allowClick(uuid))
    }

    @Test
    fun testCloseWaitingScreen() {
        val guard = DialogClickGuard()
        val player = mockk<Player>(relaxed = true)
        every { player.uniqueId } returns UUID.randomUUID()

        // chưa bấm nút WAIT_FOR_RESPONSE thì không đóng inventory (player có thể đang mở GUI khác)
        guard.onClick(player, AfterAction.CLOSE)
        guard.closeWaitingScreen(player)
        verify(exactly = 0) { player.closeInventory() }

        guard.onClick(player, AfterAction.WAIT_FOR_RESPONSE)
        guard.closeWaitingScreen(player)
        guard.closeWaitingScreen(player)
        verify(exactly = 1) { player.closeInventory() }

        // dialog mới thay màn chờ
        guard.onClick(player, AfterAction.WAIT_FOR_RESPONSE)
        guard.onShow(player)
        guard.closeWaitingScreen(player)
        verify(exactly = 1) { player.closeInventory() }
    }

    @Test
    fun testLock() {
        val guard = DialogClickGuard()
        val uuid = UUID.randomUUID()
        val player = mockk<Player>(relaxed = true)
        every { player.uniqueId } returns uuid

        guard.lock(uuid)
        assertTrue(guard.isLocked(uuid))
        // player khác không bị ảnh hưởng
        assertFalse(guard.isLocked(UUID.randomUUID()))

        guard.unlock(uuid)
        assertFalse(guard.isLocked(uuid))

        // dialog mới thay dialog đang khoá
        guard.lock(uuid)
        guard.onShow(player)
        assertFalse(guard.isLocked(uuid))

        guard.lock(uuid)
        guard.forget(uuid)
        assertFalse(guard.isLocked(uuid))
    }

    @Test
    fun testLockedDialogIgnoresClick() {
        val guard = DialogClickGuard()
        val sessions = CallbackSessions(mockk(relaxed = true), guard)
        val uuid = UUID.randomUUID()
        val player = mockk<Player>(relaxed = true)
        every { player.uniqueId } returns uuid
        var clicks = 0
        val handler: (DialogResponse) -> Unit = { clicks++ }
        sessions.put(player, CallbackSessions.Session("token", "~s~", emptyList(), listOf(handler), AfterAction.NONE, emptySet()))

        fun click() {
            val event = mockk<PlayerCommandPreprocessEvent>(relaxed = true)
            every { event.message } returns "/${DialogEncoder.CALLBACK_COMMAND} token.0"
            every { event.player } returns player
            sessions.onCommand(event)
        }

        click()
        assertEquals(1, clicks)

        guard.lock(uuid)
        click()
        assertEquals(1, clicks)

        guard.unlock(uuid)
        click()
        assertEquals(2, clicks)
    }
}
