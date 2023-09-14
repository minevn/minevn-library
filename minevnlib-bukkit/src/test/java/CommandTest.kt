import io.mockk.every
import io.mockk.mockk
import net.minevn.libs.bukkit.command
import org.bukkit.command.Command
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CommandTest {
	@Test
	fun commandTreeTest() {
		val result = arrayOf("none", "none", "none")

		val cmd = command {
			addSubCommand("childcmd1") {
				addSubCommand("childcmd2") {
					action {
						result[0] = commandTree
					}
				}

				action {
					result[1] = commandTree
				}
			}

			action {
				result[2] = commandTree
			}
		}

		val bukkitCmd: Command = mockk {
			every { name } returns  "rootcmd"
		}

		cmd.onCommand(mockk(), bukkitCmd, "", arrayOf("childcmd1", "childcmd2"))
		cmd.onCommand(mockk(), bukkitCmd, "", arrayOf("childcmd1"))
		cmd.onCommand(mockk(), bukkitCmd, "", arrayOf())

		assertEquals("rootcmd childcmd1 childcmd2", result[0])
		assertEquals("rootcmd childcmd1", result[1])
		assertEquals("rootcmd", result[2])
	}
}