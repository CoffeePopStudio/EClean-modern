package top.e404.eclean.platform

import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.plugin.Plugin

class PaperSchedulerFacade(
    private val plugin: Plugin
) : SchedulerFacade {
    override fun runGlobal(task: () -> Unit) {
        plugin.server.scheduler.runTask(plugin, Runnable(task))
    }

    override fun runAsync(task: () -> Unit) {
        plugin.server.scheduler.runTaskAsynchronously(plugin, Runnable(task))
    }

    override fun runAtLocation(location: Location, task: () -> Unit) {
        runGlobal(task)
    }

    override fun runForEntity(entity: Entity, task: () -> Unit) {
        runGlobal(task)
    }

    override fun runLaterGlobal(delayTicks: Long, task: () -> Unit): SchedulerHandle {
        val scheduled = plugin.server.scheduler.runTaskLater(plugin, Runnable(task), delayTicks)
        return SchedulerHandle { scheduled.cancel() }
    }

    override fun runLaterForEntity(entity: Entity, delayTicks: Long, task: () -> Unit): SchedulerHandle {
        return runLaterGlobal(delayTicks, task)
    }

    override fun scheduleRepeatingGlobal(delayTicks: Long, periodTicks: Long, task: () -> Unit): SchedulerHandle {
        val scheduled = plugin.server.scheduler.runTaskTimer(plugin, Runnable(task), delayTicks, periodTicks)
        return SchedulerHandle { scheduled.cancel() }
    }
}
