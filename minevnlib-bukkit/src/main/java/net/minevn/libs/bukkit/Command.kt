package net.minevn.libs.bukkit

import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.plugin.java.JavaPlugin

class Command : TabExecutor {
	private var description: String? = null
	private var usage: String = ""
	private val subCommands = mutableMapOf<String, Command>()
	private var onCommand: CommandAction.() -> Unit = {
		sendSubCommandsUsage(sender) ?: run {
			sender.sendMessage("Command action not set.")
		}
	}
	private var onTabComplete: CommandAction.() -> List<String> =
		{
			val index = args.size - 1
			val arg = args.lastOrNull()
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

	private fun ((CommandSender, Array<String>) -> Unit).toCommandAction(): CommandAction.() -> Unit {
		return { this@toCommandAction(this.sender, this.args) }
	}

	/**
	 * The action this command will do,
	 * when no sub-command matched.
	 *
	 * If not set, the default action will be sending usage of
	 * sub-commands or itself.
	 */
	fun onCommand(callBack: (sender: CommandSender, args: Array<String>) -> Unit) = this.apply {
		onCommand = callBack.toCommandAction()
	}

	/**
	 * The action this command will do,
	 * when no sub-command matched.
	 *
	 * If not set, the default action will be sending usage of
	 * sub-commands or itself.
	 */
	fun action(callBack: CommandAction.() -> Unit) = this.apply {
		onCommand = callBack
	}

	private fun ((CommandSender, Array<String>) -> List<String>).toTabCompleteAction(): CommandAction.() -> List<String> {
		return { this@toTabCompleteAction(this.sender, this.args) }
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
	fun onTabComplete(callBack: (CommandSender, Array<String>) -> List<String>) = this.apply {
		onTabComplete = callBack.toTabCompleteAction()
	}

	fun tabComplete(callBack: CommandAction.() -> List<String>) = this.apply {
		onTabComplete = callBack
	}

	/**
	 * Add a sub-command
	 *
	 * @param command The sub-command
	 * @param aliases The aliases of this sub-command
	 */
	fun addSubCommand(command: Command, vararg aliases: String) = this.apply {
		aliases.forEach { subCommands[it] = command }
	}

	/**
	 * Add a sub-command
	 *
	 * @param aliases The aliases of this sub-command
	 * @param command The sub-command
	 */
	fun addSubCommand(vararg aliases: String, command: Command.() -> Unit) = this.apply {
		addSubCommand(Command().apply(command), *aliases)
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

	fun onTabComplete(
		sender: CommandSender,
		command: org.bukkit.command.Command?,
		alias: String?,
		commandTree: String,
		args: Array<String>
	): List<String> = subCommands[args.firstOrNull()]
		?.onTabComplete(sender, command, alias, "$commandTree ${args[0]}", args.drop(1).toTypedArray())
		?: onTabComplete(CommandAction(sender, args, commandTree))

	override fun onTabComplete(
		sender: CommandSender,
		command: org.bukkit.command.Command?,
		alias: String?,
		args: Array<String>
	): List<String> = onTabComplete(sender, command, alias, command!!.name, args)

	fun onCommand(
		sender: CommandSender,
		command: org.bukkit.command.Command?,
		label: String?,
		commandTree: String,
		args: Array<String>
	): Boolean {
		subCommands[args.firstOrNull()]
			?.onCommand(sender, command, label, "$commandTree ${args[0]}", args.drop(1).toTypedArray())
			?: onCommand(CommandAction(sender, args, commandTree))
		return true
	}

	override fun onCommand(
		sender: CommandSender,
		command: org.bukkit.command.Command?,
		label: String?,
		args: Array<String>
	) = onCommand(sender, command, label, command!!.name, args)

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

/**
 * The action of a command
 *
 * @param sender The sender of this command
 * @param args The arguments of this command
 * @param commandTree No idea how to explain this ¯\_(ツ)_/¯
 */
class CommandAction(val sender: CommandSender, val args: Array<String>, val commandTree: String)

/**
 * The action of a tab complete
 *
 * @param sender The sender of this command
 * @param index The index of current argument. If you
 *   request for suggestions when typing `/foo bar baz`,
 *   the index is 1
 * @param arg The current (last) agrument,
 *   `/foo bar baz` will be `baz`
 */
class TabCompleteAction(val sender: CommandSender, val index: Int, val arg: String?)

fun command(block: Command.() -> Unit) = Command().apply(block)
