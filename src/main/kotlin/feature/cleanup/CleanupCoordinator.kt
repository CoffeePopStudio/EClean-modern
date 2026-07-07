package top.e404.eclean.feature.cleanup

import top.e404.eclean.clean.cleanDenseEntities
import top.e404.eclean.clean.cleanDrop
import top.e404.eclean.clean.cleanLiving
import top.e404.eclean.config.Config
import top.e404.eclean.platform.SchedulerFacade
import top.e404.eclean.service.StatusSnapshotService
import top.e404.eplugin.EPlugin

class CleanupCoordinator(
    private val plugin: EPlugin,
    private val scheduler: SchedulerFacade,
    private val announcements: CleanupAnnouncementService,
    private val snapshots: StatusSnapshotService,
) {
    fun cleanNow() {
        plugin.debug { "通过 CleanupCoordinator 触发一次完整清理" }
        cleanDrop(announce = true)
        cleanLiving(announce = true)
        cleanDenseEntities(announce = true)
        snapshots.updateCleanup {
            it.copy(
                elapsedSeconds = 0,
                remainingSeconds = Config.config.duration,
            )
        }
    }
}
