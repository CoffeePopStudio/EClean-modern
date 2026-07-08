package top.e404.eclean.platform

import org.bukkit.Location
import org.bukkit.entity.Entity

interface SchedulerFacade {
    fun runGlobal(task: () -> Unit)
    fun runAsync(task: () -> Unit)
    fun runAtLocation(location: Location, task: () -> Unit)
    fun runForEntity(entity: Entity, task: () -> Unit)
    fun runLaterGlobal(delayTicks: Long, task: () -> Unit): SchedulerHandle?
    fun runLaterForEntity(entity: Entity, delayTicks: Long, task: () -> Unit): SchedulerHandle?
    fun scheduleRepeatingGlobal(delayTicks: Long, periodTicks: Long, task: () -> Unit): SchedulerHandle?
    fun cancelPluginTasks()
}

fun interface SchedulerHandle {
    fun cancel()
}
