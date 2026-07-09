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
    private var task: ScheduledTask? = null
    private var elapsed: Long = 0

    fun start() {
        stop()
        val worlds = Bukkit.getWorlds().map { it.name }
        val intervals = worlds.mapNotNull { name ->
            val entry = Config.current.perWorld.worlds[name]
            if (entry?.enabled == false) return@mapNotNull null
            entry?.intervalSeconds ?: Config.current.cleanup.intervalSeconds
        }
        val interval = intervals.ifEmpty { listOf(Config.current.cleanup.intervalSeconds) }.min()
        elapsed = 0
        snapshots.updateCleanup {
            it.copy(elapsedSeconds = 0, remainingSeconds = interval)
        }
        task = Schedulers.scheduleRepeatingGlobal(20, 20) {
            val e = elapsed + 1
            elapsed = e
            val remaining = (interval - e).coerceAtLeast(0)
            snapshots.updateCleanup { it.copy(elapsedSeconds = e, remainingSeconds = remaining) }
            announcements.announceCountdown(remaining)
            if (e >= interval) {
                elapsed = 0
                coordinator.cleanNow()
            }
        }
        messages.info("Cleanup ticker started (interval=${interval}s, ${worlds.size} worlds)")
    }

    val elapsedSeconds: Long
        get() = elapsed

    fun stop() {
        task?.cancel()
        task = null
        elapsed = 0
    }
}
