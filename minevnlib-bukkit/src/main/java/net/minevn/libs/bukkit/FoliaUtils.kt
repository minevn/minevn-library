package net.minevn.libs.bukkit

import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

/**
 * Utility methods for scheduling Bukkit tasks on both Folia and non-Folia servers.
 *
 * Available functions:
 * - [isFolia]: checks whether the server is running on Folia.
 * - [runNextTick]: runs a sync task on the next tick.
 * - [runGlobal]: backward compatibility API for [runNextTick].
 * - [runAsync]: runs an async task immediately.
 * - [runLater]: runs a sync task after a delay in ticks.
 * - [runLaterAsync]: runs an async task after a delay in ticks.
 * - [runAsyncLater]: backward compatibility API for [runLaterAsync].
 * - [runTimer]: runs a repeating sync task.
 * - [runTimerAsync]: runs a repeating async task.
 * - [runAsyncTimer]: backward compatibility API for [runTimerAsync].
 */
object FoliaUtils {
    /**
     * Check if the server is running on Folia
     */
    @JvmStatic
    fun isFolia(): Boolean {
        return try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer")
            true
        } catch (_: ClassNotFoundException) {
            false
        }
    }

    /**
     * Run a task on the next tick
     */
    @JvmStatic
    fun runNextTick(plugin: JavaPlugin, runnable: Runnable): BukkitTask {
        return if (isFolia()) {
            val scheduler = Bukkit.getServer().javaClass.getMethod("getGlobalRegionScheduler").invoke(Bukkit.getServer())
            val method = scheduler.javaClass.getMethod("run", Plugin::class.java, Consumer::class.java)
            val task = method.invoke(
                scheduler,
                plugin,
                Consumer<Any> { runnable.run() }
            )
            wrapTask(plugin, task, true)
        } else {
            Bukkit.getScheduler().runTask(plugin, runnable)
        }
    }

    /**
     * Backward compatibility API for [runNextTick].
     *
     * Runs a task on the global region scheduler.
     */
    @JvmStatic
    fun runGlobal(plugin: JavaPlugin, runnable: Runnable): BukkitTask {
        return runNextTick(plugin, runnable)
    }

    /**
     * Run a task asynchronously
     */
    @JvmStatic
    fun runAsync(plugin: JavaPlugin, runnable: Runnable): BukkitTask {
        return if (isFolia()) {
            val scheduler = Bukkit.getServer().javaClass.getMethod("getAsyncScheduler").invoke(Bukkit.getServer())
            val method = scheduler.javaClass.getMethod("runNow", Plugin::class.java, Consumer::class.java)
            val task = method.invoke(scheduler, plugin, Consumer<Any> { runnable.run() })
            wrapTask(plugin, task, false)
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable)
        }
    }

    /**
     * Run a task after a delay
     */
    @JvmStatic
    fun runLater(plugin: JavaPlugin, runnable: Runnable, delay: Long): BukkitTask {
        return if (isFolia()) {
            val scheduler = Bukkit.getServer().javaClass.getMethod("getGlobalRegionScheduler").invoke(Bukkit.getServer())
            val method = scheduler.javaClass.getMethod(
                "runDelayed",
                Plugin::class.java,
                Consumer::class.java,
                Long::class.javaPrimitiveType
            )
            val task = method.invoke(scheduler, plugin, Consumer<Any> { runnable.run() }, delay)
            wrapTask(plugin, task, true)
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, runnable, delay)
        }
    }

    /**
     * Run a task asynchronously after a delay.
     */
    @JvmStatic
    fun runLaterAsync(plugin: JavaPlugin, runnable: Runnable, delay: Long): BukkitTask {
        return if (isFolia()) {
            val scheduler = Bukkit.getServer().javaClass.getMethod("getAsyncScheduler").invoke(Bukkit.getServer())
            val method = scheduler.javaClass.getMethod(
                "runDelayed",
                Plugin::class.java,
                Consumer::class.java,
                Long::class.javaPrimitiveType,
                TimeUnit::class.java
            )
            val task = method.invoke(
                scheduler,
                plugin,
                Consumer<Any> { runnable.run() },
                delay * 50L,
                TimeUnit.MILLISECONDS
            )
            wrapTask(plugin, task, false)
        } else {
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, runnable, delay)
        }
    }

    /**
     * Backward compatibility API for [runLaterAsync].
     *
     * Runs a task asynchronously after a delay.
     */
    @JvmStatic
    fun runAsyncLater(plugin: JavaPlugin, runnable: Runnable, delay: Long): BukkitTask {
        return runLaterAsync(plugin, runnable, delay)
    }

    /**
     * Run a repeating task
     */
    @JvmStatic
    fun runTimer(plugin: JavaPlugin, runnable: Runnable, delay: Long, period: Long): BukkitTask {
        return if (isFolia()) {
            val scheduler = Bukkit.getServer().javaClass.getMethod("getGlobalRegionScheduler").invoke(Bukkit.getServer())
            val method = scheduler.javaClass.getMethod(
                "runAtFixedRate",
                Plugin::class.java,
                Consumer::class.java,
                Long::class.javaPrimitiveType,
                Long::class.javaPrimitiveType
            )
            val task = method.invoke(scheduler, plugin, Consumer<Any> { runnable.run() }, delay, period)
            wrapTask(plugin, task, true)
        } else {
            Bukkit.getScheduler().runTaskTimer(plugin, runnable, delay, period)
        }
    }

    /**
     * Run a repeating task asynchronously.
     */
    @JvmStatic
    fun runTimerAsync(plugin: JavaPlugin, runnable: Runnable, delay: Long, period: Long): BukkitTask {
        return if (isFolia()) {
            val scheduler = Bukkit.getServer().javaClass.getMethod("getAsyncScheduler").invoke(Bukkit.getServer())
            val method = scheduler.javaClass.getMethod(
                "runAtFixedRate",
                Plugin::class.java,
                Consumer::class.java,
                Long::class.javaPrimitiveType,
                Long::class.javaPrimitiveType,
                TimeUnit::class.java
            )
            val task = method.invoke(
                scheduler,
                plugin,
                Consumer<Any> { runnable.run() },
                delay * 50L,
                period * 50L,
                TimeUnit.MILLISECONDS
            )
            wrapTask(plugin, task, false)
        } else {
            Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, runnable, delay, period)
        }
    }

    /**
     * Backward compatibility API for [runTimerAsync].
     *
     * Runs a repeating task asynchronously.
     */
    @JvmStatic
    fun runAsyncTimer(plugin: JavaPlugin, runnable: Runnable, delay: Long, period: Long): BukkitTask {
        return runTimerAsync(plugin, runnable, delay, period)
    }

    private fun wrapTask(plugin: JavaPlugin, task: Any, sync: Boolean): BukkitTask {
        return object : BukkitTask {
            override fun getTaskId(): Int = -1
            override fun isSync(): Boolean = sync
            override fun isCancelled(): Boolean = false
            override fun getOwner(): Plugin = plugin
            override fun cancel() {
                val method = task.javaClass.getMethod("cancel")
                method.isAccessible = true
                method.invoke(task)
            }
        }
    }
}
