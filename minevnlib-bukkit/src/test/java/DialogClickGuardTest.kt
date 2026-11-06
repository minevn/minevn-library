import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.minevn.libs.bukkit.dialog.AfterAction
import net.minevn.libs.bukkit.dialog.DialogClickGuard
import org.bukkit.entity.Player
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
}
