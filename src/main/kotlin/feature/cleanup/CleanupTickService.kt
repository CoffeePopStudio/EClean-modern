package top.e404.eclean.feature.cleanup

import top.e404.eclean.config.Config
import top.e404.eclean.platform.SchedulerFacade
import top.e404.eclean.platform.SchedulerHandle
import top.e404.eclean.service.StatusSnapshotService
import top.e404.eplugin.EPlugin

class CleanupTickService(
    private val plugin: EPlugin,
    private val scheduler: SchedulerFacade,
    private val coordinator: CleanupCoordinator,
    private val announcements: CleanupAnnouncementService,
    private val snapshots: StatusSnapshotService,
) {
    private var task: SchedulerHandle? = null

    var elapsedSeconds = 0L
        private set

    fun start() {
        stop()
        elapsedSeconds = 0
        val duration = Config.current.cleanup.intervalSeconds
        snapshots.updateCleanup {
            it.copy(elapsedSeconds = 0, remainingSeconds = duration)
        }
        task = scheduler.scheduleRepeatingGlobal(20, 20) {
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
        plugin.info("&f设置 modern 清理任务, 间隔${duration}秒")
    }

    fun stop() {
        task?.cancel()
        task = null
    }
}
