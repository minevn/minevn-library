package net.minevn.libs.bukkit

import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.plugin.java.JavaPlugin

class Command : TabExecutor {
	private var description: String? = null
	private var usage: String = ""
	private val subCommands = mutableMapOf<String, Command>()
	private var onCommand: (CommandSender, Array<String>) -> Unit = { sender, _ ->
		val seen = mutableListOf<Command>()
		sendSubCommandsUsage(sender) ?: run {
			sender.sendMessage("Command action not set.")
		}
	}
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

	/**
	 * Set description of this command
	 */
	fun description(value: String) = this.apply {
		description = value
	}

	/**
	 * Set usage of this command
	 */
	fun usage(value: String) = this.apply {
		usage = value
	}

	/**
	 * The action this command will do,
	 * when no sub-command matched.
	 *
	 * If not set, the default action will be sending usage of
	 * sub-commands or itself.
	 */
	fun onCommand(callBack: (CommandSender, Array<String>) -> Unit) = this.apply {
		onCommand = callBack
	}

	/**
	 * List of agrument this command will suggest,
	 * when no sub-command matched.
	 *
	 * Function parameter:
	 * - CommandSender
	 * - Int: The index of current argument. If you
	 *   request for suggestions when typing `/foo bar baz`,
	 *   the index is 1
	 * - String: The current (last) agrument,
	 *   `/foo bar baz` will be `baz`
	 */
	fun onTabComplete(callBack: (CommandSender, Int, String?) -> List<String>) = this.apply {
		onTabComplete = callBack
	}

	fun addSubCommand(command: Command, vararg aliases: String) = this.apply {
		aliases.forEach { subCommands[it] = command }
	}

	fun sendSubCommandsUsage(sender: CommandSender) = run {
		val seen = mutableListOf<Command>()
		subCommands.takeIf { it.isNotEmpty() }?.forEach {
			val cmd = it.value
			if (seen.contains(cmd)) return@forEach
			seen.add(cmd)
			val name = it.key
			sender.sendMessage("§e$name $usage - $description")
		}
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

	/**
	 * Register the command
	 *
	 * @param plugin The plugin to register this command to
	 * @param command The command's name, must be registered in
	 * `plugin.yml`
	 */
	fun register(plugin: JavaPlugin, command: String) {
		plugin.getCommand(command)?.let {
			it.executor = this
			it.tabCompleter = this
			plugin.logger.info("Registered command $command")
		}
	}
}
