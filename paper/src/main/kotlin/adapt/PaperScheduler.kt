package top.e404.eclean.paper.adapt

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.plugin.Plugin
import top.e404.eclean.common.api.CommonLocation
import top.e404.eclean.common.api.ScheduledTask
import top.e404.eclean.common.api.Scheduler
import java.util.UUID
import java.util.function.Consumer

class PaperScheduler(
    private val plugin: Plugin,
) : Scheduler {

    override fun runGlobal(task: () -> Unit) {
        Bukkit.getGlobalRegionScheduler().execute(plugin, Runnable(task))
    }

    override fun runAsync(task: () -> Unit) {
        Bukkit.getAsyncScheduler().runNow(plugin, Consumer { task() })
    }

    override fun runAtRegion(location: CommonLocation, task: () -> Unit) {
        val world = Bukkit.getWorld(location.worldName) ?: return
        val bukkitLocation = Location(world, location.x, location.y, location.z)
        Bukkit.getRegionScheduler().execute(plugin, bukkitLocation, Runnable(task))
    }

    override fun runForEntity(entityId: String, task: () -> Unit) {
        val entity = runCatching { UUID.fromString(entityId) }
            .getOrNull()
            ?.let { Bukkit.getEntity(it) }
            ?: return
        entity.scheduler.run(plugin, Consumer { task() }, null)
    }

    override fun runLaterGlobal(delayTicks: Long, task: () -> Unit): ScheduledTask? {
        val scheduled = Bukkit.getGlobalRegionScheduler()
            .runDelayed(plugin, Consumer { task() }, delayTicks)
        return object : ScheduledTask {
            override fun cancel() {
                scheduled.cancel()
            }
        }
    }

    override fun runLaterForEntity(
        entityId: String,
        delayTicks: Long,
        task: () -> Unit,
    ): ScheduledTask? {
        val entity = runCatching { UUID.fromString(entityId) }
            .getOrNull()
            ?.let { Bukkit.getEntity(it) }
            ?: return null
        val scheduled = entity.scheduler.runDelayed(plugin, Consumer { task() }, null, delayTicks)
            ?: return null
        return object : ScheduledTask {
            override fun cancel() {
                scheduled.cancel()
            }
        }
    }

    override fun scheduleRepeatingGlobal(
        delayTicks: Long,
        periodTicks: Long,
        task: () -> Unit,
    ): ScheduledTask? {
        val scheduled = Bukkit.getGlobalRegionScheduler()
            .runAtFixedRate(plugin, Consumer { task() }, delayTicks, periodTicks)
        return object : ScheduledTask {
            override fun cancel() {
                scheduled.cancel()
            }
        }
    }

    override fun cancelAll() {
        Bukkit.getGlobalRegionScheduler().cancelTasks(plugin)
        Bukkit.getAsyncScheduler().cancelTasks(plugin)
    }
}
