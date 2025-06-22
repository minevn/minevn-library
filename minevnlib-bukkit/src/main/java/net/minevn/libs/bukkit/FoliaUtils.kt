package net.minevn.libs.bukkit

import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask
import java.util.concurrent.TimeUnit

object FoliaUtils {
    /**
     * Check if the server is running on Folia
     */
    @JvmStatic
    fun isFolia(): Boolean {
        return try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer")
            true
        } catch (e: ClassNotFoundException) {
            false
        }
    }

    /**
     * Run a task asynchronously
     */
    @JvmStatic
    fun runAsync(plugin: JavaPlugin, runnable: Runnable): BukkitTask {
        return if (isFolia()) {
            val scheduler = Bukkit.getServer().javaClass.getMethod("getAsyncScheduler").invoke(Bukkit.getServer())
            val method = scheduler.javaClass.getMethod(
                "runNow",
                Plugin::class.java,
                java.util.function.Consumer::class.java)
            val task = method.invoke(scheduler, plugin, java.util.function.Consumer<Any> { runnable.run() })
            object : BukkitTask {
                override fun getTaskId(): Int = -1
                override fun isSync(): Boolean = false
                override fun isCancelled(): Boolean = false
                override fun getOwner(): Plugin = plugin
                override fun cancel() {
                    val method = task.javaClass.getMethod("cancel")
                    method.isAccessible = true
                    method.invoke(task)
                }
            }
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable)
        }
    }

    /**
     * Run a task on the global region scheduler
     */
    @JvmStatic
    fun runGlobal(plugin: JavaPlugin, runnable: Runnable): BukkitTask {
        return if (isFolia()) {
            val scheduler = Bukkit.getServer().javaClass.getMethod("getGlobalRegionScheduler").invoke(Bukkit.getServer())
            val method = scheduler.javaClass.getMethod("run", Plugin::class.java, java.util.function.Consumer::class.java)
            val task = method.invoke(
                scheduler,
                plugin,
                java.util.function.Consumer<Any> { runnable.run() })
            object : BukkitTask {
                override fun getTaskId(): Int = -1
                override fun isSync(): Boolean = true
                override fun isCancelled(): Boolean = false
                override fun getOwner(): Plugin = plugin
                override fun cancel() {
                    val method = task.javaClass.getMethod("cancel")
                    method.isAccessible = true
                    method.invoke(task)
                }
            }
        } else {
            Bukkit.getScheduler().runTask(plugin, runnable)
        }
    }

    /**
     * Run a repeating task asynchronously
     */
    @JvmStatic
    fun runAsyncTimer(plugin: JavaPlugin, runnable: Runnable, delay: Long, period: Long): BukkitTask {
        return if (isFolia()) {
            val scheduler = Bukkit.getServer().javaClass.getMethod("getAsyncScheduler").invoke(Bukkit.getServer())
            val method = scheduler.javaClass.getMethod(
                "runAtFixedRate",
                Plugin::class.java,
                java.util.function.Consumer::class.java,
                Long::class.javaPrimitiveType,
                Long::class.javaPrimitiveType,
                TimeUnit::class.java
            )
            val task = method.invoke(
                scheduler,
                plugin,
                java.util.function.Consumer<Any> { runnable.run() },
                delay * 50L,
                period * 50L,
                TimeUnit.MILLISECONDS
            )
            object : BukkitTask {
                override fun getTaskId(): Int = -1
                override fun isSync(): Boolean = false
                override fun isCancelled(): Boolean = false
                override fun getOwner(): Plugin = plugin
                override fun cancel() {
                    val method = task.javaClass.getMethod("cancel")
                    method.isAccessible = true
                    method.invoke(task)
                }
            }
        } else {
            Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, runnable, delay, period)
        }
    }
}
