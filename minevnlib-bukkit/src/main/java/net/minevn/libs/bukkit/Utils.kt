package net.minevn.libs.bukkit

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import java.util.*

class ViaVersionNotInstalledException(cause: Throwable? = null) : IllegalStateException(
    "ViaVersion not found.",
    cause,
)

fun List<String>.color() = map { it.color() }

fun String.color(): String = ChatColor.translateAlternateColorCodes('&', this)

fun runSync(r: Runnable) {
    if (Bukkit.isPrimaryThread()) {
        r.run()
    } else {
        if (FoliaUtils.isFolia()) {
            FoliaUtils.runGlobal(MineVNLib.instance, r)
        } else {
            Bukkit.getScheduler().runTask(MineVNLib.instance, r)
        }
    }
}

fun Player.sendMessages(messages: List<String>) = messages.forEach { sendMessage(it) }

fun Player.sendMessages(messages: Array<String>) = messages.forEach { sendMessage(it) }

fun Player.getClientProtocolVersion(): Int {
    try {
        val viaClass = Class.forName("com.viaversion.viaversion.api.Via")
        val api = viaClass.getMethod("getAPI").invoke(null)
        val getPlayerVersion = api.javaClass.getMethod("getPlayerVersion", UUID::class.java)
        return (getPlayerVersion.invoke(api, uniqueId) as Number).toInt()
    } catch (e: ReflectiveOperationException) {
        throw ViaVersionNotInstalledException(e)
    } catch (e: IllegalStateException) {
        throw ViaVersionNotInstalledException(e)
    }
}

val colorCodes = "abcdefklmr0123456789".toList()

fun String.split(maxLength: Int) = mutableListOf<String>().apply {
    var current: StringBuilder? = null
    var colorLength = 0

    this@split.forEachIndexed { index, char ->
        if (current == null) {
            current = StringBuilder().append(char)
        } else {
            current!!.append(char)
        }

        if (colorCodes.contains(char) && index > 0 && this@split[index - 1] == '§') {
            colorLength += 2
        }

        if (!char.isWhitespace()) {
            return@forEachIndexed
        }

        if (char == '\n' || current!!.length - colorLength >= maxLength) {
            addWithLastColor(current.toString())
            current = null
            colorLength = 0
        }
    }

    current?.let { addWithLastColor(current.toString()) }
}

fun MutableList<String>.addWithLastColor(string: String) {
    val lastColor = if (isEmpty()) "" else ChatColor.getLastColors(last())
    add("$lastColor${string.trim()}")
}

fun Location.asString() = "${world.name},$x,$y,$z,$yaw,$pitch"

fun String.asLocation() = split(",").let {
    val world = Bukkit.getWorld(it[0]) ?: run {
        val logger = MineVNLib.instance.logger
        logger.warning("World not found: ${it[0]}")
        logger.warning("Check following stacktrace to fix:")
        Thread.dumpStack()
        return@let null
    }
    val x = it[1].toDouble()
    val y = it[2].toDouble()
    val z = it[3].toDouble()
    val yaw = it[4].toFloat()
    val pitch = it[5].toFloat()
    Location(world, x, y, z, yaw, pitch)
}

fun ItemMeta.hideAll() {
    ItemFlag.entries.forEach { addItemFlags(it) }
}

/**
 * ItemMeta.setCustomModelData chỉ có từ 1.14, trong khi thư viện biên dịch với API 1.13.2,
 * nên phải gọi qua reflection. Bằng null nếu server cũ hơn 1.14.
 */
private val setCustomModelDataMethod = try {
    ItemMeta::class.java.getMethod("setCustomModelData", Int::class.javaObjectType)
} catch (_: NoSuchMethodException) {
    null
}

/**
 * Gán model cho icon theo custom model data
 */
fun ItemStack.setIconData(iconData: Short) = apply {
    val method = setCustomModelDataMethod
    if (iconData <= 0 || method == null) {
        return@apply
    }
    val im = itemMeta
    method.invoke(im, iconData.toInt())
    itemMeta = im
}
