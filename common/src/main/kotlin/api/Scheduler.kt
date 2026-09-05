package top.e404.eclean.common.api

/**
 * Loader-agnostic scheduler. Implementations map these calls to Paper/Folia,
 * Fabric, or NeoForge scheduling APIs.
 */
interface Scheduler {
    fun runGlobal(task: () -> Unit)
    fun runAsync(task: () -> Unit)
    fun runAtRegion(location: CommonLocation, task: () -> Unit)
    fun runForEntity(entityId: String, task: () -> Unit)
    fun runLaterGlobal(delayTicks: Long, task: () -> Unit): ScheduledTask?
    fun runLaterForEntity(entityId: String, delayTicks: Long, task: () -> Unit): ScheduledTask?
    fun scheduleRepeatingGlobal(delayTicks: Long, periodTicks: Long, task: () -> Unit): ScheduledTask?
    fun cancelAll()
}

interface ScheduledTask {
    fun cancel()
}
