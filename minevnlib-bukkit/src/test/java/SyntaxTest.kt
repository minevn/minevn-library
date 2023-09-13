import net.minevn.libs.bukkit.command
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

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
					when(index) {
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
}