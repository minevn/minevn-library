package net.minevn.lib.bukkit

import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.plugin.java.JavaPlugin

class Command : TabExecutor {
	private val subCommands = mutableMapOf<String, Command>()
	private var onCommand: (CommandSender, Array<String>) -> Unit = { _, _ -> }
	private var onTabComplete: (CommandSender, Int, String?) -> List<String> =
		{ _, index, arg ->
			if (index == 0) {
				subCommands.keys
					.filter { arg.isNullOrEmpty() || it.startsWith(arg) }
					.sorted()
			} else {
				emptyList()
			}
		}

	fun onCommand(callBack: (CommandSender, Array<String>) -> Unit) = this.apply {
		onCommand = callBack
	}

	fun onTabComplete(callBack: (CommandSender, Int, String?) -> List<String>) = this.apply {
		onTabComplete = callBack
	}

	fun addSubCommand(command: Command, vararg aliases: String) = this.apply {
		aliases.forEach { subCommands[it] = command }
	}

	override fun onTabComplete(
		sender: CommandSender,
		command: org.bukkit.command.Command?,
		alias: String?,
		args: Array<String>
	): List<String> = subCommands[args.firstOrNull()]
		?.onTabComplete(sender, command, alias, args.drop(1).toTypedArray())
		?: onTabComplete(sender, args.size - 1, args.lastOrNull())

	override fun onCommand(
		sender: CommandSender,
		command: org.bukkit.command.Command?,
		label: String?,
		args: Array<String>
	): Boolean {
		subCommands[args.firstOrNull()]
			?.onCommand(sender, command, label, args.drop(1).toTypedArray())
			?: onCommand(sender, args)
		return true
	}

	fun register(plugin: JavaPlugin, command: String) {
		plugin.getCommand(command)?.let {
			it.executor = this
			it.tabCompleter = this
			plugin.logger.info("Registered command $command")
		}
	}
}
