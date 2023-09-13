import io.mockk.every
import io.mockk.mockk
import net.minevn.libs.bukkit.command
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.plugin.java.JavaPlugin
import org.junit.jupiter.api.Test

class SyntaxTest {
	fun dslTest() {
		command {
			addSubCommand("test") {
				description("Test command")
				usage("/test")

				action {
					sender.sendMessage("destroy tokyo ")
				}

				tabComplete {
					when(args.size) {
						1 -> listOf("hail", "putin")
						else -> emptyList()
					}
				}
			}

			action {
				sender.sendMessage("no subcommand matched")
			}

			register(Bukkit.getPluginManager().getPlugin("somePlugin") as JavaPlugin, "hehe")
		}
	}

	@Test
	fun what() {
		val cmd = command {
			addSubCommand("test") {
				addSubCommand("what") {
					action {
						println(commandTree)
					}
				}

				action {
					println(commandTree)
				}
			}

			action {
				println("root")
			}
		}

		val bukkitCmd: Command = mockk {
			every { name } returns  "hehe"
		}

		cmd.onCommand(mockk(), bukkitCmd, "", arrayOf("test", "what"))
	}
}