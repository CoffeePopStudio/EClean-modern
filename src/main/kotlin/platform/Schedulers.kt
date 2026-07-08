package top.e404.eclean.platform

import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.plugin.Plugin
import top.e404.eclean.app.RuntimeServices
import java.util.function.Consumer

object Schedulers {
    private val plugin: Plugin get() = RuntimeServices.plugin

    fun runGlobal(task: () -> Unit) {
        Bukkit.getGlobalRegionScheduler().execute(plugin, Runnable(task))
    }

    fun runAsync(task: () -> Unit) {
        Bukkit.getAsyncScheduler().runNow(plugin, consumer(task))
    }

    fun runAtLocation(location: Location, task: () -> Unit) {
        Bukkit.getRegionScheduler().execute(plugin, location, Runnable(task))
    }

    fun runForEntity(entity: Entity, task: () -> Unit) {
        entity.scheduler.run(plugin, consumer(task), null)
    }

    fun runLaterGlobal(delayTicks: Long, task: () -> Unit): ScheduledTask? {
        return Bukkit.getGlobalRegionScheduler().runDelayed(plugin, consumer(task), delayTicks)
    }

    fun runLaterForEntity(entity: Entity, delayTicks: Long, task: () -> Unit): ScheduledTask? {
        return entity.scheduler.runDelayed(plugin, consumer(task), null, delayTicks)
    }

    fun scheduleRepeatingGlobal(delayTicks: Long, periodTicks: Long, task: () -> Unit): ScheduledTask? {
        return Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, consumer(task), delayTicks, periodTicks)
    }

    fun cancelPluginTasks() {
        Bukkit.getGlobalRegionScheduler().cancelTasks(plugin)
        Bukkit.getAsyncScheduler().cancelTasks(plugin)
    }

    private fun consumer(task: () -> Unit): Consumer<ScheduledTask> = Consumer { task() }
}
