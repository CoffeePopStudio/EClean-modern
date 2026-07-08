package top.e404.eclean.feature.cleanup

import io.papermc.paper.threadedregions.scheduler.ScheduledTask
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

    var elapsedSeconds = 0L
        private set

    fun start() {
        stop()
        elapsedSeconds = 0
        val duration = Config.current.cleanup.intervalSeconds
        snapshots.updateCleanup {
            it.copy(elapsedSeconds = 0, remainingSeconds = duration)
        }
        task = Schedulers.scheduleRepeatingGlobal(20, 20) {
            elapsedSeconds++
            val remaining = (duration - elapsedSeconds).coerceAtLeast(0)
            snapshots.updateCleanup {
                it.copy(
                    elapsedSeconds = elapsedSeconds,
                    remainingSeconds = remaining,
                )
            }
            announcements.announceCountdown(remaining)
            if (elapsedSeconds >= duration) {
                elapsedSeconds = 0
                coordinator.cleanNow()
            }
        }
        messages.info("Modern cleanup ticker started (interval=${duration}s)")
    }

    fun stop() {
        task?.cancel()
        task = null
    }
}
