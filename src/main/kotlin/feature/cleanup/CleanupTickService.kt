package top.e404.eclean.feature.cleanup

import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import org.bukkit.Bukkit
import top.e404.eclean.config.Config
import top.e404.eclean.platform.Schedulers
import top.e404.eclean.service.StatusSnapshotService
import top.e404.eclean.app.MessageService

class CleanupTickService(
    private val messages: MessageService,
    private val coordinator: CleanupCoordinator,
    private val announcements: CleanupAnnouncementService,
    private val snapshots: StatusSnapshotService,
) {
    private val tasks = mutableMapOf<String, ScheduledTask?>()
    private val elapsed = mutableMapOf<String, Long>()

    fun start() {
        stop()
        val worlds = Bukkit.getWorlds().map { it.name }
        for (worldName in worlds) {
            val entry = Config.current.perWorld.worlds[worldName]
            if (entry?.enabled == false) continue
            startWorld(worldName, entry?.intervalSeconds)
        }
        messages.info("Per-world cleanup tickers started (${tasks.size} worlds)")
    }

    private fun startWorld(worldName: String, overrideInterval: Long?) {
        val interval = overrideInterval ?: Config.current.cleanup.intervalSeconds
        elapsed[worldName] = 0
        tasks[worldName] = Schedulers.scheduleRepeatingGlobal(20, 20) {
            val e = (elapsed[worldName] ?: 0) + 1
            elapsed[worldName] = e
            val remaining = (interval - e).coerceAtLeast(0)
            announcements.announceCountdown(remaining)
            if (e >= interval) {
                elapsed[worldName] = 0
                coordinator.cleanNow()
            }
        }
    }

    val elapsedSeconds: Long
        get() = elapsed["world"] ?: 0L

    fun stop() {
        tasks.values.forEach { it?.cancel() }
        tasks.clear()
        elapsed.clear()
    }
}
