package top.e404.eclean.platform

import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.plugin.Plugin
import java.util.function.Consumer

class FoliaSchedulerFacade(
    private val plugin: Plugin
) : SchedulerFacade {
    override fun runGlobal(task: () -> Unit) {
        plugin.server.globalRegionScheduler.execute(plugin, Runnable(task))
    }

    override fun runAsync(task: () -> Unit) {
        plugin.server.asyncScheduler.runNow(plugin, consumer(task))
    }

    override fun runAtLocation(location: Location, task: () -> Unit) {
        plugin.server.regionScheduler.execute(plugin, location, Runnable(task))
    }

    override fun runForEntity(entity: Entity, task: () -> Unit) {
        entity.scheduler.run(plugin, consumer(task), null)
    }

    override fun runLaterGlobal(delayTicks: Long, task: () -> Unit): SchedulerHandle? {
        return handle(plugin.server.globalRegionScheduler.runDelayed(plugin, consumer(task), delayTicks))
    }

    override fun runLaterForEntity(entity: Entity, delayTicks: Long, task: () -> Unit): SchedulerHandle? {
        return handle(entity.scheduler.runDelayed(plugin, consumer(task), null, delayTicks))
    }

    override fun scheduleRepeatingGlobal(delayTicks: Long, periodTicks: Long, task: () -> Unit): SchedulerHandle? {
        return handle(plugin.server.globalRegionScheduler.runAtFixedRate(plugin, consumer(task), delayTicks, periodTicks))
    }

    override fun cancelPluginTasks() {
        plugin.server.globalRegionScheduler.cancelTasks(plugin)
        plugin.server.asyncScheduler.cancelTasks(plugin)
    }

    private fun consumer(task: () -> Unit): Consumer<ScheduledTask> = Consumer { task() }

    private fun handle(rawTask: ScheduledTask?): SchedulerHandle? {
        if (rawTask == null) return null
        return SchedulerHandle { rawTask.cancel() }
    }
}
